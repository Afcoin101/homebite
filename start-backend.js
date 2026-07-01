const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const logFile = path.join(__dirname, 'backend.log');
const out = fs.openSync(logFile, 'a');
const err = fs.openSync(logFile, 'a');

console.log('Spawning backend/server.js...');
const child = spawn('node', [path.join(__dirname, 'backend', 'server.js')], {
  detached: true,
  stdio: [ 'ignore', out, err ]
});

child.unref();
console.log('✓ Backend server spawned in background with PID:', child.pid);
process.exit(0);
