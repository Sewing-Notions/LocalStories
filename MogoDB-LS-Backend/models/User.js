// models/User.js
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  username: String,
  email: String
});

module.exports = mongoose.model('User', userSchema);
