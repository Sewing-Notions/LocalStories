// models/Story.js
const mongoose = require('mongoose');

const storySchema = new mongoose.Schema({
  storyId: { type: String, required: true, unique: true },
  title: String,
  description: String,
  dateOfFact: Date,
  photoPath: String,
  locationId: String,
  userId: String
});

module.exports = mongoose.model('Story', storySchema);

