// models/Report.js
const mongoose = require('mongoose');

const reportSchema = new mongoose.Schema({
  reportId: { type: String, required: true, unique: true },
  userId: String,
  reason: String,
  reportDate: Date,
  storyId: String
});

module.exports = mongoose.model('Report', reportSchema);
