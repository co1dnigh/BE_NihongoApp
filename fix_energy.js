const fs=require('fs');
const p='D:/123/BE_NihongoApp/src/main/java/com/example/nihongo_app/service/EnergyService.java';
let c=fs.readFileSync(p,'utf8');
// Replace the mangled recoverEnergy block
c=c.replace(
  /LocalDate today = LocalDate\.now\(\);[\s\S]*?user\.setLastEnergyResetDate\(today\);/,
  'LocalDateTime now = LocalDateTime.now();\n    LocalDateTime lastReset = user.getLastEnergyResetDate();\n\n    if (lastReset == null) {\n      user.setLastEnergyResetDate(now);\n      userRepository.save(user);\n      return;\n    }\n\n    long elapsedSeconds = Duration.between(lastReset, now).getSeconds();\n    int recoveryCount = (int) (elapsedSeconds / RECOVERY_INTERVAL_SECONDS);\n\n    if (recoveryCount > 0) {\n      int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);\n      int maxEnergy = Objects.requireNonNullElse(user.getMaxEnergy(), MAX_ENERGY);\n\n      if (currentEnergy < maxEnergy) {\n        int newEnergy = Math.min(currentEnergy + recoveryCount, maxEnergy);\n        user.setCurrentEnergy(newEnergy);\n      }\n\n      user.setLastEnergyResetDate(now.minusSeconds((int)(elapsedSeconds % RECOVERY_INTERVAL_SECONDS)));');
c=c.replace('import java.time.LocalDate;','');
c=c.replace('import java.util.concurrent.ThreadLocalRandom;','import java.util.concurrent.ThreadLocalRandom;\nimport java.time.LocalDateTime;\nimport java.time.Duration;');
fs.writeFileSync(p,c,'utf8');
console.log('EnergyService fixed');
