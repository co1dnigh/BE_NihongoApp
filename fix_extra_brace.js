const fs = require('fs');
const p = 'src/main/java/com/example/nihongo_app/service/impl/LessonAttemptServiceImpl.java';
let c = fs.readFileSync(p, 'utf8');
c = c.replace(/ \}\n\n \/\//, '\n\n //');
fs.writeFileSync(p, c, 'utf8');
console.log('Removed extra }');
