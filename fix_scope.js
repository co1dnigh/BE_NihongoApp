const fs = require('fs');
const p = 'D:/123/BE_NihongoApp/src/main/java/com/example/nihongo_app/service/impl/LessonAttemptServiceImpl.java';
const c = fs.readFileSync(p, 'utf8');
const lines = c.split('\n');
let insertIdx = -1;

const marker = "expGained = Math.max(1, (int) Math.round(expGained * ratio));";
for (let i = 0; i < lines.length; i++) {
  if (lines[i].trim() === marker) {
    insertIdx = i + 1;
    break;
  }
}

console.log('Insert close at index', insertIdx);
if (insertIdx > 0) {
  lines.splice(insertIdx, 0, ' }');
  fs.writeFileSync(p, lines.join('\n'), 'utf8');
  console.log('Done. Closed isReplay.');
} else {
  console.log('Marker not found');
}
