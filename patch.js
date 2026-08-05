const fs=require('fs');
const p='D:/123/BE_NihongoApp/src/main/java/com/example/nihongo_app/service/EnergyService.java';
let c=fs.readFileSync(p,'utf8');
c=c.replace('private static final int MAX_ENERGY = 25;',
  'private static final int MAX_ENERGY = 25;\n  private static final int RECOVERY_INTERVAL_SECONDS = 3600;\n  private static final int AD_COOLDOWN_SECONDS = 1800;');
// Fix recoverEnergy: change LocalDate to LocalDateTime + Duration logic
c=c.replace(/LocalDate today = LocalDate\.now\(\);/,'LocalDateTime now = LocalDateTime.now();');
c=c.replace(/LocalDate lastReset = user\.getLastEnergyResetDate\(\);/,'LocalDateTime lastReset = user.getLastEnergyResetDate();');
c=c.replace(/import java\.time\.LocalDate;.*$/m,'');
c=c.replace(/import java\.util\.Objects;\n/,'import java.time.Duration;\nimport java.util.concurrent.ThreadLocalRandom;\nimport java.util.Objects;\n');
// Fix recoverEnergy body - days to seconds
c=c.replace(/long daysBetween = java\.time\.temporal\.ChronoUnit\.DAYS\.between\(lastReset, today\);\n\s*int recoveryCount = \(int\) daysBetween;/,
  'long elapsedSeconds = Duration.between(lastReset, now).getSeconds();\n    int recoveryCount = (int) (elapsedSeconds / RECOVERY_INTERVAL_SECONDS);');
c=c.replace(/user\.setLastEnergyResetDate\(today\);/,
  'user.setLastEnergyResetDate(now.minusSeconds((int)(elapsedSeconds % RECOVERY_INTERVAL_SECONDS)));');
// Remove duplicate RECOVERY_INTERVAL_SECONDS if exists
c=c.replace(/(RECOVERY_INTERVAL_SECONDS = 3600;\n\s*){2}/,'RECOVERY_INTERVAL_SECONDS = 3600;\n  ');
fs.writeFileSync(p,c,'utf8');
console.log('EnergyService patched');
