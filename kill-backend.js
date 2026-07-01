const { exec } = require('child_process');

console.log('Searching for running node server.js processes...');
exec('ps aux', (err, stdout, stderr) => {
  if (err) {
    console.error('Failed to run ps aux:', err);
    process.exit(1);
  }
  
  const lines = stdout.split('\n');
  const myPid = process.pid;
  let killedAny = false;
  
  lines.forEach(line => {
    if (line.includes('node') && line.includes('server.js') && !line.includes('kill-backend.js')) {
      const parts = line.trim().split(/\s+/);
      const pid = parseInt(parts[1], 10);
      if (pid && pid !== myPid) {
        try {
          process.kill(pid, 'SIGKILL');
          console.log(`✓ Force-killed hanging backend process with PID: ${pid}`);
          killedAny = true;
        } catch (e) {
          console.error(`Failed to kill process ${pid}:`, e.message);
        }
      }
    }
  });
  
  if (!killedAny) {
    console.log('No other running server.js processes found.');
  }
  process.exit(0);
});
