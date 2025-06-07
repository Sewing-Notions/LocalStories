// models/Location.js
const mongoose = require('mongoose');

const locationSchema = new mongoose.Schema({
  locationId: { type: String, required: true, unique: true },
  name: String,
  latitude: Number,
  longitude: Number
});

module.exports = mongoose.model('Location', locationSchema);

