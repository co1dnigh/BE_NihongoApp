const fs = require('fs');
let p = 'D:/123/BE_NihongoApp/src/main/java/com/example/nihongo_app/service/impl/LessonAttemptServiceImpl.java';
let c = fs.readFileSync(p, 'utf8');
let lines = c.split('\n');

// Find line 183 (0-indexed 182): "}" closing the switch
// Find line 184: empty
// Find line 185: "// Replay: giam EXP..."
// Find line 186: "// JUMP_TEST replay..."
// Find line 187: "if (isReplay && expGained > 0) {"
// Find line 188: "double ratio = resolveReplayExpRatio(lesson);"
// Find line 189: "expGained = Math.max(1, (int) Math.round(expGained * ratio));"
// Find line 190: "" (empty)
// Find line 191: "// Tinh nang luong..." <- energy block starts HERE (inside if)
// Find line 231: "}" <- closes the energy block (also closes isReplay if)
// Line 232: "" empty
// Line 233: "// Cap nhat progress..."

// Strategy: remove lines 191-231 (energy block) from inside the isReplay block
// Then insert it AFTER line 190 (after replay ratio block)

// First, let's verify exact content
console.log('Lines 183-235:');
for (let i = 182; i < 235 && i < lines.length; i++) {
  console.log(i + 1, JSON.stringify(lines[i]));
}
