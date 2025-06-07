
// models/CompassUsage.js
const mongoose = require('mongoose');

const compassUsageSchema = new mongoose.Schema({
  locationName: String,
  userId: String,
  timestamp: Date,
  heading: Number
});

module.exports = mongoose.model('CompassUsage', compassUsageSchema);
