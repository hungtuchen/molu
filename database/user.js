/*
{
  token,
  hardId,
  username,
  password,
  gender
}
*/
/*
{
  "status": "accepted",
  "destination": {
    "latitude": 40.1518294095,
    "eta": 48,
    "longitude": 116.3076806426
  },
  "request_id": "4414aa1d-9e5c-45e4-b11d-3acdf722e98e",
"driver": {
"phone_number": "+8618601950772",
    "rating": 4.88,
    "picture_url": "https://d297l2q4lq2ras.cloudfront.net/nomad/2015/5/8/15/abb4eb9b-c256-4f24-8be7-5d61f0eedef2.jpeg",
    "name": "亚光"
  },
  "pickup": {
    "latitude": 40.0521414308,
    "eta": 7,
    "longitude": 116.2935613222
  },
  "eta": 7,
  "location": {
    "latitude": 40.0570839799,
    "bearing": 152,
    "longitude": 116.3035753525
  },
  "vehicle": {
    "make": "现代",
    "picture_url": null,
    "model": "朗动",
    "license_plate": "京q5hu87"
  },
  "surge_multiplier": 1
}
*/

var users = {

};

function getUserByHardId(id) {
  return users[id];
}

function createNewUser(userFields) {
  var hardId = userFields.hardId;
  users[hardId] = userFields;
};

module.exports = {
  getUserByHardId,
  createNewUser
};
