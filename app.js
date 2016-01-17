var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var config = require('config');
var session = require('express-session');
var request = require('superagent');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Application Settings
var clientId = config.get('uber.client_id');
var clientSecret = config.get('uber.client_secret');
var port = config.get('port') || 3000;
var redirect_host = config.get('uber.redirect_host');
var redirect_path = config.get('uber.redirect_path');

var User = require('./database/user');
var Riding = require('./database/riding');
var Message = require('./database/message');

app.use(session({
    secret: config.get('secret'),
    resave: false,
    saveUninitialized: true
}));

var oauth2 = require('simple-oauth2')({
    clientID: config.get('uber.client_id'),
    clientSecret: config.get('uber.client_secret'),
    site: 'https://login.uber.com.cn',
    tokenPath: '/oauth/v2/token',
    authorizationPath: '/oauth/v2/authorize'
});

// Authorization uri definition
var authorization_uri = oauth2.authCode.authorizeURL({
    redirect_uri: redirect_host + ":" + port + redirect_path,
    scope: config.get('uber.scopes'),
    state: '3(#0/!~'
});

// Initial page redirecting to Uber
app.get('/auth', function (req, res) {
    res.redirect(authorization_uri);
});

// Callback service parsing the authorization token and asking for the access token
app.get(redirect_path, function (req, res) {
    var code = req.query.code;
    oauth2.authCode.getToken({
        code: code,
        redirect_uri: redirect_host + ":" + port + redirect_path
    }, saveToken);

    function saveToken(error, token) {
        if (error) {
            console.log('Access Token Error', error.message);
        }
        var accessToken = oauth2.accessToken.create(token);
        res.send(accessToken);
    }
});


// /UberServer/Request?token=helloworld&hardId=100&start_latitude=40.05215&start_longitude=116.293571&end_latitude=39.947458&end_longitude=116.359758
/*
{
"status": "in_progress",
"destination": {
  "latitude": 39.9474480065,
  "eta": 13,
  "longitude": 116.3597458684
},
"request_id": "4aff0ddc-725d-4eab-ad4a-0d9a9c95ca5a",
"driver": {
  "phone_number": "(555)555-5555",
  "rating": 4.9,
  "picture_url": "https://static.uberx.net.cn/uberex-sandbox/images/driver.jpg",
  "name": "John"
},
"pickup": {
  "latitude": 40.0521414308,
  "longitude": 116.2935613222
},
"eta": null,
"location": {
  "latitude": 40.051425676,
  "bearing": 154,
  "longitude": 116.2940141682
},
"vehicle": {
  "make": "Toyota",
  "picture_url": "https://static.uberx.net.cn/uberex-sandbox/images/prius.jpg",
  "model": "Prius",
  "license_plate": "UBER-PLATE"
},
"surge_multiplier": 1
}
*/
app.get('/UberServer/Request', function(req, res) {
  var productId = '6bf8dc3b-c8b0-4f37-9b61-579e64016f7a'; // default to People's Uber
  var hardId = req.query.hardId;
  var token = req.query.token;
  var start_latitude = req.query.start_latitude;
  var start_longitude = req.query.start_longitude;
  var end_latitude = req.query.end_latitude;
  var end_longitude = req.query.end_longitude;
  request
    .post('https://sandbox-api.uber.com.cn/v1/requests')
    .set('Content-Type', 'application/json')
    .set('Authorization', 'Bearer ' + token)
    .send({product_id: productId, start_latitude, start_longitude, end_latitude, end_longitude })
    .end(function(err, response){
        if (err || !response.ok) {
          res.status(err.status).json({ message: err.message });
        } else {
          Riding.addNewRiding({ hardId, start_latitude, start_longitude, end_latitude, end_longitude});
          res.status(200).json(response.body);
        }
   });
});
// /UberServer/HardIds?hardId=100
app.get('/UberServer/HardIds', function(req, res){
  var hardId = req.query.hardId;
  res.status(200).json(Riding.findOthers(hardId));
});
// /UberServer/SendMessage?msg=”hello world”&toHardId=100&fromHardId=101
app.get('/UberServer/SendMessage', function(req, res) {
  console.log('message', req.query);
  var message = req.query;
  var response = Message.addNewMessage(message);
  res.status(200).json(response);
});
// /UberServer/RecvMessage?hardId=100
app.get('/UberServer/RecvMessage', function(req, res) {
  var hardId = req.query.hardId;
  res.status(200).json(Message.getMessagesByHardId(hardId));
});


app.get('/', function (req, res) {
    res.send('Hello<br><a href="/auth">Connect With uber</a>');
});

app.listen(port);

console.log("Listening on " + redirect_host + ":" + port);
