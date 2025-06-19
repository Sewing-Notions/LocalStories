const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');

// Load models
const User = require('./models/User');
const Location = require('./models/Location');
const Story = require('./models/Story');
const Report = require('./models/Report');
const CompassUsage = require('./models/CompassUsage');

// Connect to MongoDB
mongoose.connect('mongodb://localhost:27017/local_stories', {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  console.log('Connected to MongoDB');
  seedData();
}).catch(err => {
  console.error('MongoDB connection error:', err);
});

async function seedData() {
  try {
    const data = JSON.parse(fs.readFileSync(path.join(__dirname, 'database.json'), 'utf-8'));

    await User.insertMany(data.User);
    await Location.insertMany(data.Location);
    await Story.insertMany(data.Story);
    await Report.insertMany(data.Report);
    await CompassUsage.insertMany(data.CompassUsage);

    console.log('Database seeded successfully!');
    mongoose.disconnect();
  } catch (err) {
    console.error('Error seeding database:', err);
    mongoose.disconnect();
  }
}

