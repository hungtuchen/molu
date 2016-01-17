/*
{
  hardId:
    {
      start_latitude,
      start_longitude,
      end_latitude,
      end_longitude
    }
}
*/
var ridings = {

};

function addNewRiding(ridingFields) {
  var hardId = ridingFields.hardId;
  ridings[hardId] = ridingFields;
  console.log('ridings', ridings);
}

function findOthers(hardId) {
  var otherIds = Object.keys(ridings);
  return otherIds.map((id) => ridings[id]);
  console.log('ridings', ridings);
}

module.exports = {
  addNewRiding,
  findOthers,
};
