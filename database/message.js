/*
to: [{
  fromHardId,
  toHardId,
  content
}]
*/

var messages = {

};

function addNewMessage(message) {
  var toId = message.toHardId;
  if (!messages[toId]) {
    messages[toId] = [message];
  } else {
    messages[toId].push(message);
  }
  console.log('messages', messages);
  return { status: 'ok' };
}

function getMessagesByHardId(hardId) {
  return messages[hardId];
  console.log('messages', messages);
}

module.exports = {
  addNewMessage,
  getMessagesByHardId,
};
