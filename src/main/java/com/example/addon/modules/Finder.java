/* Decompiler 728ms, total 1188ms, lines 1108 */
package com.larp.debug.modules;

import com.larp.debug.AddonTemplate;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2604;
import net.minecraft.class_2611;
import net.minecraft.class_2622;
import net.minecraft.class_2626;
import net.minecraft.class_2636;
import net.minecraft.class_2663;
import net.minecraft.class_2672;
import net.minecraft.class_2675;
import net.minecraft.class_2680;
import net.minecraft.class_2716;
import net.minecraft.class_2751;
import net.minecraft.class_2767;
import net.minecraft.class_2818;
import net.minecraft.class_3414;

public class Finder extends Module {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> onlyOnBedrock;
   private final Setting<Boolean> antiEspMode;
   private final Setting<Boolean> playerBasedDetection;
   private final Setting<Boolean> serverSideBypass;
   private final Setting<Boolean> strictValidation;
   private final Setting<Boolean> requirePlayerPresence;
   private final Setting<Boolean> playerPacketDetection;
   private final Setting<Boolean> detectOnBedrock;
   private final Setting<Boolean> detectUnderDeepslate;
   private final Setting<Boolean> packetAntiEspBypass;
   private final Setting<Boolean> showPendingChunks;
   private final Setting<Integer> scanDelay;
   private final Setting<Integer> maxRetries;
   private final Setting<Finder.RenderMode> renderMode;
   private final Setting<Integer> flatRenderY;
   private final Set<class_1923> spawnerChunks;
   private final Set<class_1923> pendingChunks;
   private final Set<class_1923> playerChunks;
   private final Set<class_2338> playerPositions;
   private final Set<Integer> flaggedEntities;
   private final Set<class_1923> entityChunks;
   private final Set<class_1923> redstoneChunks;
   private final Set<class_1923> activeBaseChunks;
   private final Set<class_1923> greenBaseChunks;
   private final Set<class_2338> redstoneActivity;
   private final Set<class_1923> particleActivity;
   private final Set<class_1923> soundActivity;
   private final Set<class_1923> storageChunks;
   private final Map<class_1923, Long> lastActivityTime;

   public Finder() {
      super(AddonTemplate.CATEGORY, "LarpDebug V7", "Markiert Chunks mit verdaechtigen Aktivitaeten.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.onlyOnBedrock = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("only-on-bedrock")).description("Nur Spawner auf Bedrock-Hoehe (Y=0) erkennen.")).defaultValue(true)).build());
      this.antiEspMode = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("anti-esp-mode")).description("Anti-ESP Fix: Verwendet mehrere Detection-Methoden.")).defaultValue(true)).build());
      this.playerBasedDetection = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("player-based-detection")).description("Player-basierte Detection (umgeht Anti-ESP).")).defaultValue(true)).build());
      this.serverSideBypass = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("server-side-bypass")).description("Server-seitige Anti-ESP Umgehung.")).defaultValue(false)).build());
      this.strictValidation = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("strict-validation")).description("Strikte Validierung um false positives zu vermeiden.")).defaultValue(false)).build());
      this.requirePlayerPresence = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("require-player-presence")).description("Benoetigt Player in der Naehe fuer Detection.")).defaultValue(false)).build());
      this.playerPacketDetection = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("player-packet-detection")).description("Player-Packet Detection fuer Bedrock/Deepslate.")).defaultValue(true)).build());
      this.detectOnBedrock = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("detect-on-bedrock")).description("Player auf Bedrock (Y <= -64) detektieren.")).defaultValue(true)).build());
      this.detectUnderDeepslate = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("detect-under-deepslate")).description("Player unter Deepslate (Y < 0) detektieren.")).defaultValue(true)).build());
      this.packetAntiEspBypass = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("packet-anti-esp-bypass")).description("Verwendet Packet-Detection fuer Anti-ESP Bypass.")).defaultValue(true)).build());
      this.showPendingChunks = this.sgGeneral.add(((Builder)((Builder)((Builder)(new Builder()).name("show-pending-chunks")).description("Zeigt gelbe Chunks waehrend des Scannens an.")).defaultValue(true)).build());
      this.scanDelay = this.sgGeneral.add(((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)(new meteordevelopment.meteorclient.settings.IntSetting.Builder()).name("scan-delay")).description("Verzoegerung zwischen Scans in ms (Anti-ESP).")).defaultValue(100)).min(50).max(1000).sliderMax(1000).build());
      this.maxRetries = this.sgGeneral.add(((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)(new meteordevelopment.meteorclient.settings.IntSetting.Builder()).name("max-retries")).defaultValue(3)).min(1).max(10).sliderMax(10).build());
      this.renderMode = this.sgGeneral.add(((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)(new meteordevelopment.meteorclient.settings.EnumSetting.Builder()).name("render-mode")).description("Kies tussen pillar of flat chunk rendering.")).defaultValue(Finder.RenderMode.Pillar)).build());
      this.flatRenderY = this.sgGeneral.add(((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)(new meteordevelopment.meteorclient.settings.IntSetting.Builder()).name("flat-render-y")).description("Y-level voor flat chunk render.")).defaultValue(64)).range(-64, 320).visible(() -> {
         return this.renderMode.get() == Finder.RenderMode.Flat;
      })).build());
      this.spawnerChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.pendingChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.playerChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.playerPositions = Collections.newSetFromMap(new ConcurrentHashMap());
      this.flaggedEntities = Collections.newSetFromMap(new ConcurrentHashMap());
      this.entityChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.redstoneChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.activeBaseChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.greenBaseChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.redstoneActivity = Collections.newSetFromMap(new ConcurrentHashMap());
      this.particleActivity = Collections.newSetFromMap(new ConcurrentHashMap());
      this.soundActivity = Collections.newSetFromMap(new ConcurrentHashMap());
      this.storageChunks = Collections.newSetFromMap(new ConcurrentHashMap());
      this.lastActivityTime = new ConcurrentHashMap();
   }

   public void onActivate() {
      this.clearCache();
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(Post event) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         try {
            if (this.mc.field_1687.method_75260() % 20L == 0L) {
               this.scanForHiddenSpawners();
            }
         } catch (Exception var3) {
         }
      }

   }

   private void scanForHiddenSpawners() {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         try {
            class_1923 playerChunk = this.mc.field_1724.method_31476();
            int range = 5;

            for(int dx = -range; dx <= range; ++dx) {
               label98:
               for(int dz = -range; dz <= range; ++dz) {
                  class_1923 chunkPos = new class_1923(playerChunk.field_9181 + dx, playerChunk.field_9180 + dz);
                  if (this.mc.field_1687.method_2935().method_12123(chunkPos.field_9181, chunkPos.field_9180)) {
                     class_2818 chunk = this.mc.field_1687.method_8497(chunkPos.field_9181, chunkPos.field_9180);
                     if (chunk != null) {
                        Iterator var7 = chunk.method_12214().values().iterator();

                        while(var7.hasNext()) {
                           class_2586 blockEntity = (class_2586)var7.next();
                           if (blockEntity instanceof class_2636) {
                              class_2636 spawner = (class_2636)blockEntity;
                              int spawnerY = spawner.method_11016().method_10264();
                              if ((!(Boolean)this.onlyOnBedrock.get() || spawnerY == 0 || (Boolean)this.packetAntiEspBypass.get() && spawnerY <= 0) && (!(Boolean)this.strictValidation.get() || this.isValidSpawner(spawner))) {
                                 this.spawnerChunks.add(chunkPos);
                                 break;
                              }
                           }
                        }

                        var7 = this.mc.field_1687.method_18456().iterator();

                        while(true) {
                           class_1657 player;
                           do {
                              do {
                                 if (!var7.hasNext()) {
                                    continue label98;
                                 }

                                 player = (class_1657)var7.next();
                              } while(player == null);
                           } while(!player.method_31476().equals(chunkPos));

                           double playerY = player.method_23318();
                           boolean shouldDetect = false;
                           if ((Boolean)this.detectOnBedrock.get() && playerY <= -64.0D) {
                              shouldDetect = true;
                           } else if ((Boolean)this.detectUnderDeepslate.get() && playerY < 0.0D) {
                              shouldDetect = true;
                           } else if ((Boolean)this.packetAntiEspBypass.get() && playerY <= 0.0D) {
                              shouldDetect = true;
                           }

                           if (shouldDetect) {
                              this.playerChunks.add(chunkPos);
                           }
                        }
                     }
                  }
               }
            }
         } catch (Exception var12) {
         }
      }

   }

   private void checkEntity(class_1297 entity) {
      if (entity != null && this.mc.field_1687 != null) {
         try {
            class_2338 pos = entity.method_24515();
            class_2338 belowPos = pos.method_10074();
            if (this.mc.field_1687.method_8320(belowPos).method_27852(class_2246.field_28888) || this.mc.field_1687.method_8320(belowPos).method_27852(class_2246.field_9987)) {
               this.flagEntity(entity);
            }
         } catch (Exception var4) {
         }
      }

   }

   private void flagEntity(class_1297 entity) {
      if (entity != null) {
         try {
            if (!this.flaggedEntities.contains(entity.method_5628())) {
               this.flaggedEntities.add(entity.method_5628());
               class_1923 chunkPos = new class_1923(entity.method_24515());
               this.entityChunks.add(chunkPos);
            }
         } catch (Exception var3) {
         }
      }

   }

   private void handleEntitySpawn(class_2604 packet) {
      try {
         if (this.mc.field_1687 != null) {
            Iterator var2 = this.mc.field_1687.method_18112().iterator();

            while(var2.hasNext()) {
               class_1297 entity = (class_1297)var2.next();
               if (entity != null && !this.flaggedEntities.contains(entity.method_5628())) {
                  this.checkEntity(entity);
               }
            }
         }
      } catch (Exception var4) {
      }

   }

   private void handleRedstonePacket(class_2626 packet) {
      try {
         class_2338 pos = packet.method_11309();
         class_2680 state = packet.method_11308();
         if (this.isRedstoneActive(state)) {
            this.redstoneActivity.add(pos);
            class_1923 chunkPos = new class_1923(pos);
            this.redstoneChunks.add(chunkPos);
            this.addBaseChunk(pos);
         }
      } catch (Exception var5) {
      }

   }

   private void handleParticlePacket(class_2675 packet) {
      try {
         double x = packet.method_11544();
         double z = packet.method_11546();
         class_2338 pos = new class_2338((int)x, (int)packet.method_11547(), (int)z);
         class_1923 chunkPos = new class_1923(pos);
         if (this.isSuspiciousParticle(packet)) {
            this.particleActivity.add(chunkPos);
            this.addBaseChunk(pos);
         }
      } catch (Exception var8) {
      }

   }

   private void handleSoundPacket(class_2767 packet) {
      try {
         double x = packet.method_11890();
         double z = packet.method_11893();
         class_2338 pos = new class_2338((int)x, (int)packet.method_11889(), (int)z);
         class_1923 chunkPos = new class_1923(pos);
         if (this.isSuspiciousSound(packet)) {
            this.soundActivity.add(chunkPos);
            this.addBaseChunk(pos);
         }
      } catch (Exception var8) {
      }

   }

   private void updateActivityTime(class_1923 chunkPos) {
      this.lastActivityTime.put(chunkPos, System.currentTimeMillis());
   }

   private void addBaseChunk(class_2338 pos) {
      class_1923 normalChunk;
      try {
         class_2338 aboveBasePos = new class_2338(pos.method_10263(), pos.method_10264() + 5, pos.method_10260());
         normalChunk = new class_1923(aboveBasePos);
         this.greenBaseChunks.clear();
         this.greenBaseChunks.add(normalChunk);
         this.activeBaseChunks.clear();
         this.activeBaseChunks.add(normalChunk);
         this.updateActivityTime(normalChunk);
      } catch (Exception var4) {
         normalChunk = new class_1923(pos);
         this.greenBaseChunks.clear();
         this.greenBaseChunks.add(normalChunk);
         this.activeBaseChunks.clear();
         this.activeBaseChunks.add(normalChunk);
      }

   }

   private boolean isSuspiciousParticle(class_2675 packet) {
      try {
         String particleType = packet.method_11551().method_10295().toString().toLowerCase();
         return !particleType.contains("portal") && !particleType.contains("end") && !particleType.contains("chest") && !particleType.contains("ender") && !particleType.contains("shulker") ? particleType.contains("redstone") || particleType.contains("dust") || particleType.contains("happy_villager") || particleType.contains("composter") : false;
      } catch (Exception var3) {
         return false;
      }
   }

   private boolean isSuspiciousSound(class_2767 packet) {
      try {
         String soundName = ((class_3414)packet.method_11894().comp_349()).toString().toLowerCase();
         return soundName.contains("hopper") || soundName.contains("piston") || soundName.contains("dispenser") || soundName.contains("dropper") || soundName.contains("furnace") || soundName.contains("redstone");
      } catch (Exception var3) {
         return false;
      }
   }

   private boolean isRedstoneActive(class_2680 state) {
      try {
         return state.method_27852(class_2246.field_10002) || state.method_27852(class_2246.field_10523) || state.method_27852(class_2246.field_10450) || state.method_27852(class_2246.field_10377) || state.method_27852(class_2246.field_10560) || state.method_27852(class_2246.field_10615) || state.method_27852(class_2246.field_10312) || state.method_27852(class_2246.field_10200) || state.method_27852(class_2246.field_10228) || state.method_27852(class_2246.field_10091);
      } catch (Exception var3) {
         return false;
      }
   }

   private boolean isUnderDeepslate(class_2338 pos) {
      try {
         return pos.method_10264() < 0;
      } catch (Exception var3) {
         return false;
      }
   }

   @EventHandler
   private void onPacketReceive(Receive event) {
      try {
         if ((Boolean)this.playerPacketDetection.get() && this.mc.field_1687 != null && this.mc.field_1724 != null) {
            this.scanForPlayersInWorld();
            this.cleanupOldPlayerChunks();
         }

         if (event.packet instanceof class_2622) {
            this.handleBlockEntityUpdate((class_2622)event.packet);
         }

         if (event.packet instanceof class_2672) {
            this.handleChunkDataPacket((class_2672)event.packet);
         }

         if ((Boolean)this.packetAntiEspBypass.get() && event.packet instanceof class_2626) {
            this.handleRedstonePacket((class_2626)event.packet);
         }

         if ((Boolean)this.packetAntiEspBypass.get() && event.packet instanceof class_2675) {
            this.handleParticlePacket((class_2675)event.packet);
         }

         if ((Boolean)this.packetAntiEspBypass.get() && event.packet instanceof class_2767) {
            this.handleSoundPacket((class_2767)event.packet);
         }

         if ((Boolean)this.packetAntiEspBypass.get()) {
            class_2596 var3 = event.packet;
            if (var3 instanceof class_2604) {
               class_2604 s = (class_2604)var3;
               this.handleEntitySpawn(s);
               this.applyEntitySpawnAntiEsp(s);
            }
         }

         if (event.packet instanceof class_2663) {
            this.handleEntityStatus((class_2663)event.packet);
         }

         if (event.packet instanceof class_2716) {
            this.handleEntityDestroy((class_2716)event.packet);
         }

         if (event.packet instanceof class_2751) {
            this.handleScoreboardUpdate((class_2751)event.packet);
         }

         if ((Boolean)this.antiEspMode.get() && this.mc.field_1687 != null) {
            this.scanForSpawnersDirect();
            this.cleanupOldSpawnerChunks();
         }
      } catch (Exception var4) {
      }

   }

   private void handleBlockEntityUpdate(class_2622 packet) {
      try {
         class_2338 pos = packet.method_11293();
         class_1923 chunkPos = new class_1923(pos);
         if (this.mc.field_1687 != null) {
            class_2586 var5 = this.mc.field_1687.method_8321(pos);
            if (var5 instanceof class_2636) {
               class_2636 spawner = (class_2636)var5;
               if ((Boolean)this.onlyOnBedrock.get() && spawner.method_11016().method_10264() != 0) {
                  return;
               }

               if ((Boolean)this.strictValidation.get() && !this.isValidSpawner(spawner)) {
                  return;
               }

               this.spawnerChunks.add(chunkPos);
            }
         }
      } catch (Exception var6) {
      }

   }

   private void handleEntityStatus(class_2663 packet) {
   }

   private void handleScoreboardUpdate(class_2751 packet) {
   }

   private void handleEntityDestroy(class_2716 packet) {
   }

   private void handleChunkDataPacket(class_2672 packet) {
      try {
         if (this.mc.field_1687 != null) {
            class_1923 playerChunk = this.mc.field_1724.method_31476();
            int range = 3;

            for(int dx = -range; dx <= range; ++dx) {
               for(int dz = -range; dz <= range; ++dz) {
                  class_1923 chunkPos = new class_1923(playerChunk.field_9181 + dx, playerChunk.field_9180 + dz);
                  if (this.mc.field_1687.method_2935().method_12123(chunkPos.field_9181, chunkPos.field_9180)) {
                     class_2818 chunk = this.mc.field_1687.method_8497(chunkPos.field_9181, chunkPos.field_9180);
                     if (chunk != null) {
                        this.scanChunkForSpawners(chunk, chunkPos);
                     }
                  }
               }
            }
         }
      } catch (Exception var8) {
      }

   }

   private void cleanupOldPlayerChunks() {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         try {
            this.playerChunks.removeIf((chunkPos) -> {
               Iterator var2 = this.mc.field_1687.method_18456().iterator();

               double y;
               do {
                  class_1657 player;
                  do {
                     do {
                        if (!var2.hasNext()) {
                           return true;
                        }

                        player = (class_1657)var2.next();
                     } while(player == null);
                  } while(!player.method_31476().equals(chunkPos));

                  y = player.method_23318();
               } while((!(Boolean)this.detectOnBedrock.get() || y > -64.0D) && (!(Boolean)this.detectUnderDeepslate.get() || y >= 0.0D));

               return false;
            });
            this.playerPositions.removeIf((pos) -> {
               Iterator var2 = this.mc.field_1687.method_18456().iterator();

               while(var2.hasNext()) {
                  class_1657 player = (class_1657)var2.next();
                  if (player != null) {
                     class_2338 playerPos = new class_2338((int)player.method_23317(), (int)player.method_23318(), (int)player.method_23321());
                     if (pos.equals(playerPos)) {
                        double y = player.method_23318();
                        return (!(Boolean)this.detectOnBedrock.get() || y > -64.0D) && (!(Boolean)this.detectUnderDeepslate.get() || y >= 0.0D);
                     }
                  }
               }

               return true;
            });
         } catch (Exception var2) {
            this.playerChunks.clear();
            this.playerPositions.clear();
         }
      }

   }

   private void cleanupOldSpawnerChunks() {
      if (this.mc.field_1724 != null) {
         try {
            class_1923 playerChunk = this.mc.field_1724.method_31476();
            int maxDistance = 2;
            this.spawnerChunks.removeIf((chunkPos) -> {
               int distance = Math.max(Math.abs(chunkPos.field_9181 - playerChunk.field_9181), Math.abs(chunkPos.field_9180 - playerChunk.field_9180));
               return distance > maxDistance;
            });
         } catch (Exception var3) {
         }
      }

   }

   private void scanForSpawnersDirect() {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         try {
            class_1923 playerChunkPos = this.mc.field_1724.method_31476();
            int range = 3;

            for(int dx = -range; dx <= range; ++dx) {
               for(int dz = -range; dz <= range; ++dz) {
                  class_1923 chunkPos = new class_1923(playerChunkPos.field_9181 + dx, playerChunkPos.field_9180 + dz);
                  if (this.mc.field_1687.method_2935().method_12123(chunkPos.field_9181, chunkPos.field_9180)) {
                     class_2818 chunk = this.mc.field_1687.method_8497(chunkPos.field_9181, chunkPos.field_9180);
                     if (chunk != null) {
                        this.scanChunkForSpawners(chunk, chunkPos);
                     }
                  }
               }
            }
         } catch (Exception var7) {
         }
      }

   }

   private void scanChunkForSpawners(class_2818 chunk, class_1923 chunkPos) {
      if (chunk != null) {
         try {
            Iterator var3 = chunk.method_12214().values().iterator();

            class_2636 spawner;
            int spawnerY;
            do {
               do {
                  class_2586 blockEntity;
                  do {
                     if (!var3.hasNext()) {
                        if ((Boolean)this.packetAntiEspBypass.get() && this.hasSpawnerIndicators(chunk)) {
                           this.spawnerChunks.add(chunkPos);
                        }

                        this.scanChunkForStorage(chunk, chunkPos);
                        return;
                     }

                     blockEntity = (class_2586)var3.next();
                  } while(!(blockEntity instanceof class_2636));

                  spawner = (class_2636)blockEntity;
                  spawnerY = spawner.method_11016().method_10264();
               } while((Boolean)this.onlyOnBedrock.get() && spawnerY != 0 && (!(Boolean)this.packetAntiEspBypass.get() || spawnerY > 0));
            } while((Boolean)this.strictValidation.get() && !this.isValidSpawner(spawner));

            this.spawnerChunks.removeIf((existingChunkPos) -> {
               try {
                  class_2818 existingChunk = this.mc.field_1687.method_8497(existingChunkPos.field_9181, existingChunkPos.field_9180);
                  boolean hasSpawner = false;
                  Iterator var4 = existingChunk.method_12214().values().iterator();

                  while(var4.hasNext()) {
                     class_2586 be = (class_2586)var4.next();
                     if (be instanceof class_2636) {
                        hasSpawner = true;
                        break;
                     }
                  }

                  return !hasSpawner;
               } catch (Exception var6) {
                  return true;
               }
            });
            this.spawnerChunks.add(chunkPos);
            return;
         } catch (Exception var8) {
            if ((Boolean)this.packetAntiEspBypass.get()) {
               try {
                  if (this.hasSpawnerIndicators(chunk)) {
                     this.spawnerChunks.add(chunkPos);
                  }
               } catch (Exception var7) {
               }
            }
         }
      }

   }

   private void scanForPlayersInWorld() {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         try {
            Iterator var1 = this.mc.field_1687.method_18456().iterator();

            while(true) {
               class_1657 player;
               do {
                  if (!var1.hasNext()) {
                     return;
                  }

                  player = (class_1657)var1.next();
               } while(player == null);

               double x = player.method_23317();
               double y = player.method_23318();
               double z = player.method_23321();
               boolean shouldDetect = false;
               if ((Boolean)this.detectOnBedrock.get() && y <= -64.0D) {
                  shouldDetect = true;
               } else if ((Boolean)this.detectUnderDeepslate.get() && y < 0.0D) {
                  shouldDetect = true;
               } else if ((Boolean)this.packetAntiEspBypass.get() && y <= 0.0D) {
                  shouldDetect = true;
               }

               if (shouldDetect) {
                  class_2338 pos = new class_2338((int)x, (int)y, (int)z);
                  this.playerPositions.add(pos);
                  class_1923 chunkPos = new class_1923(pos);
                  this.playerChunks.removeIf((existingChunk) -> {
                     Iterator var2 = this.mc.field_1687.method_8497(existingChunk.field_9181, existingChunk.field_9180).method_12214().values().iterator();

                     class_2586 blockEntity;
                     do {
                        if (!var2.hasNext()) {
                           return true;
                        }

                        blockEntity = (class_2586)var2.next();
                     } while(!(blockEntity instanceof class_2636));

                     return false;
                  });
                  this.playerChunks.add(chunkPos);
                  if ((Boolean)this.packetAntiEspBypass.get()) {
                     (new Thread(() -> {
                        try {
                           Thread.sleep((long)(Integer)this.scanDelay.get());
                           this.validatePlayerPosition(pos, chunkPos);
                        } catch (InterruptedException var4) {
                        }

                     })).start();
                  }
               }
            }
         } catch (Exception var12) {
         }
      }

   }

   private void validatePlayerPosition(class_2338 pos, class_1923 chunkPos) {
      if (this.mc.field_1687 != null) {
         try {
            if (this.mc.field_1687.method_8320(pos).method_27852(class_2246.field_9987) || this.mc.field_1687.method_8320(pos.method_10074()).method_27852(class_2246.field_9987) || this.mc.field_1687.method_8320(pos).method_27852(class_2246.field_28888) || this.mc.field_1687.method_8320(pos.method_10074()).method_27852(class_2246.field_28888)) {
               return;
            }

            this.playerPositions.remove(pos);
            this.playerChunks.remove(chunkPos);
         } catch (Exception var4) {
            this.playerPositions.remove(pos);
            this.playerChunks.remove(chunkPos);
         }
      }

   }

   @EventHandler
   private void onChunkData(ChunkDataEvent event) {
      class_1923 chunkPos = event.chunk().method_12004();
      this.pendingChunks.add(chunkPos);
      if ((Boolean)this.antiEspMode.get()) {
         for(int i = 0; i < (Integer)this.maxRetries.get(); ++i) {
            (new Thread(() -> {
               try {
                  Thread.sleep((long)((Integer)this.scanDelay.get() * (i + 1)));
                  if ((Boolean)this.playerBasedDetection.get()) {
                     this.scanChunkPlayerBased(event.chunk(), i);
                  } else {
                     this.scanChunkStandard(event.chunk(), i);
                  }

                  if ((Boolean)this.serverSideBypass.get() && i == (Integer)this.maxRetries.get() - 1) {
                     this.scanChunkServerBypass(event.chunk());
                  }
               } catch (InterruptedException var4) {
               }

            })).start();
         }
      } else {
         (new Thread(() -> {
            this.scanChunkStandard(event.chunk(), 0);
         })).start();
      }

   }

   private void scanChunkStandard(class_2818 chunk, int retry) {
      if (chunk != null) {
         class_1923 pos = chunk.method_12004();

         try {
            label60: {
               if ((Boolean)this.requirePlayerPresence.get() && !this.isPlayerNearChunk(pos)) {
                  return;
               }

               Iterator var4 = chunk.method_12214().values().iterator();

               class_2636 spawner;
               do {
                  do {
                     class_2586 blockEntity;
                     do {
                        if (!var4.hasNext()) {
                           break label60;
                        }

                        blockEntity = (class_2586)var4.next();
                     } while(!(blockEntity instanceof class_2636));

                     spawner = (class_2636)blockEntity;
                  } while((Boolean)this.onlyOnBedrock.get() && spawner.method_11016().method_10264() != 0);
               } while((Boolean)this.strictValidation.get() && !this.isValidSpawner(spawner));

               this.spawnerChunks.add(pos);
               this.pendingChunks.remove(pos);
               return;
            }
         } catch (Exception var7) {
         }

         if (retry == (Integer)this.maxRetries.get() - 1) {
            this.spawnerChunks.remove(pos);
            this.pendingChunks.remove(pos);
         }
      }

   }

   private void scanChunkPlayerBased(class_2818 chunk, int retry) {
      if (chunk != null) {
         class_1923 pos = chunk.method_12004();

         try {
            label53: {
               Iterator var4 = chunk.method_12214().values().iterator();

               class_2636 spawner;
               do {
                  do {
                     class_2586 blockEntity;
                     do {
                        if (!var4.hasNext()) {
                           break label53;
                        }

                        blockEntity = (class_2586)var4.next();
                     } while(!(blockEntity instanceof class_2636));

                     spawner = (class_2636)blockEntity;
                  } while((Boolean)this.onlyOnBedrock.get() && spawner.method_11016().method_10264() != 0);
               } while((Boolean)this.strictValidation.get() && !this.isValidSpawner(spawner));

               this.spawnerChunks.add(pos);
               this.pendingChunks.remove(pos);
               return;
            }
         } catch (Exception var7) {
         }

         if (retry == (Integer)this.maxRetries.get() - 1) {
            this.pendingChunks.remove(pos);
         }
      }

   }

   private void scanChunkServerBypass(class_2818 chunk) {
      if (chunk != null) {
         class_1923 pos = chunk.method_12004();

         try {
            label61: {
               int suspiciousBlocks = 0;
               int totalBlocks = 0;
               Iterator var5 = chunk.method_12214().values().iterator();

               class_2586 blockEntity;
               do {
                  do {
                     do {
                        if (!var5.hasNext()) {
                           if (totalBlocks > 0 && (double)suspiciousBlocks >= (double)totalBlocks * 0.3D) {
                              this.spawnerChunks.add(pos);
                              this.pendingChunks.remove(pos);
                              return;
                           }
                           break label61;
                        }

                        blockEntity = (class_2586)var5.next();
                        ++totalBlocks;
                     } while(blockEntity == null);

                     if (blockEntity.method_11010().method_51367() && blockEntity.method_11010().method_31709()) {
                        ++suspiciousBlocks;
                     }
                  } while(!(blockEntity instanceof class_2636));
               } while((Boolean)this.onlyOnBedrock.get() && blockEntity.method_11016().method_10264() != 0);

               this.spawnerChunks.add(pos);
               this.pendingChunks.remove(pos);
               return;
            }
         } catch (Exception var7) {
         }

         this.pendingChunks.remove(pos);
      }

   }

   private boolean hasSpawnerIndicators(class_2818 chunk) {
      try {
         int spawnerLikeCount = 0;
         int totalEntities = 0;
         Iterator var4 = chunk.method_12214().values().iterator();

         class_2586 blockEntity;
         do {
            do {
               if (!var4.hasNext()) {
                  int threshold = 1;
                  return spawnerLikeCount >= threshold && totalEntities > 0;
               }

               blockEntity = (class_2586)var4.next();
               ++totalEntities;
            } while(blockEntity == null);

            if (!(blockEntity instanceof class_2611) && !blockEntity.method_11010().method_27852(class_2246.field_10603)) {
               if ((blockEntity.method_11010().method_27852(class_2246.field_10034) || blockEntity.method_11010().method_27852(class_2246.field_10380) || blockEntity.method_11010().method_27852(class_2246.field_16328)) && blockEntity.method_11016().method_10264() < 0) {
                  this.storageChunks.add(chunk.method_12004());
               }
            } else if (blockEntity.method_11016().method_10264() < 0) {
               this.greenBaseChunks.add(chunk.method_12004());
               this.storageChunks.add(chunk.method_12004());
            }

            if (blockEntity.method_11010().method_51367() && blockEntity.method_11010().method_31709()) {
               ++spawnerLikeCount;
            }
         } while(!(blockEntity instanceof class_2636) || blockEntity.method_11016().method_10264() >= 0);

         return true;
      } catch (Exception var6) {
         return chunk.method_12214().size() > 0;
      }
   }

   private boolean isPlayerNearChunk(class_1923 pos) {
      if (this.mc.field_1724 == null) {
         return false;
      } else {
         double distance = Math.sqrt(Math.pow(this.mc.field_1724.method_23317() - (double)(pos.field_9181 * 16 + 8), 2.0D) + Math.pow(this.mc.field_1724.method_23321() - (double)(pos.field_9180 * 16 + 8), 2.0D));
         return distance < 96.0D;
      }
   }

   private boolean isValidSpawner(class_2636 spawner) {
      try {
         if (spawner == null) {
            return false;
         } else {
            class_2338 pos = spawner.method_11016();
            if (pos != null && this.mc.field_1687 != null) {
               return !this.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10260) ? false : spawner.method_11390() != null;
            } else {
               return false;
            }
         }
      } catch (Exception var3) {
         return false;
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.field_1687 != null) {
         Iterator var2 = this.spawnerChunks.iterator();

         class_1923 c;
         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(0, 255, 0, 75));
         }

         var2 = this.greenBaseChunks.iterator();

         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(0, 255, 100, 100));
         }

         var2 = this.playerChunks.iterator();

         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(0, 100, 255, 75));
         }

         var2 = this.entityChunks.iterator();

         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(255, 165, 0, 100));
         }

         var2 = this.redstoneChunks.iterator();

         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(255, 0, 0, 75));
         }

         var2 = this.activeBaseChunks.iterator();

         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(128, 0, 255, 100));
         }

         var2 = this.storageChunks.iterator();

         while(var2.hasNext()) {
            c = (class_1923)var2.next();
            this.renderChunkBox(event, c, new SettingColor(255, 215, 0, 100));
         }

         if ((Boolean)this.antiEspMode.get() && (Boolean)this.showPendingChunks.get()) {
            var2 = this.pendingChunks.iterator();

            while(var2.hasNext()) {
               c = (class_1923)var2.next();
               this.renderChunkBox(event, c, new SettingColor(255, 255, 0, 50));
            }
         }
      }

   }

   private void renderChunkBox(Render3DEvent event, class_1923 chunkPos, SettingColor color) {
      double x1 = (double)chunkPos.method_8326();
      double z1 = (double)chunkPos.method_8328();
      double x2 = (double)(chunkPos.method_8327() + 1);
      double z2 = (double)(chunkPos.method_8329() + 1);
      double y1;
      double y2;
      if (this.renderMode.get() == Finder.RenderMode.Flat) {
         y1 = (double)(Integer)this.flatRenderY.get();
         y2 = y1 + 1.0D;
      } else {
         y1 = (double)this.mc.field_1687.method_31607();
         y2 = 320.0D;
      }

      event.renderer.box(x1, y1, z1, x2, y2, z2, color, color, ShapeMode.Both, 0);
   }

   public void clearCache() {
      this.spawnerChunks.clear();
      this.pendingChunks.clear();
      this.playerChunks.clear();
      this.playerPositions.clear();
      this.flaggedEntities.clear();
      this.entityChunks.clear();
      this.redstoneChunks.clear();
      this.activeBaseChunks.clear();
      this.greenBaseChunks.clear();
      this.redstoneActivity.clear();
      this.particleActivity.clear();
      this.soundActivity.clear();
      this.storageChunks.clear();
      this.lastActivityTime.clear();
   }

   private void scanChunkForStorage(class_2818 chunk, class_1923 chunkPos) {
      if (chunk != null) {
         try {
            int storageCount = 0;
            int playerMadeCount = 0;
            Iterator var5 = chunk.method_12214().values().iterator();

            while(true) {
               class_2586 be;
               class_2680 state;
               label58:
               do {
                  while(true) {
                     while(var5.hasNext()) {
                        be = (class_2586)var5.next();
                        state = be.method_11010();
                        if (!(be instanceof class_2611) && !state.method_27852(class_2246.field_10603)) {
                           if (!state.method_27852(class_2246.field_10034) && !state.method_27852(class_2246.field_10380) && !state.method_27852(class_2246.field_16328)) {
                              continue label58;
                           }

                           ++storageCount;
                        } else {
                           ++playerMadeCount;
                           ++storageCount;
                           if (be.method_11016().method_10264() < 0) {
                              this.greenBaseChunks.add(chunkPos);
                           }
                        }
                     }

                     if (playerMadeCount >= 1 || storageCount >= 5) {
                        this.storageChunks.add(chunkPos);
                        if (playerMadeCount >= 1 && storageCount >= 2) {
                           this.activeBaseChunks.add(chunkPos);
                           return;
                        }
                     }

                     return;
                  }
               } while(!state.method_27852(class_2246.field_10312) && !state.method_27852(class_2246.field_10228));

               ++playerMadeCount;
               if (be.method_11016().method_10264() < 0) {
                  this.greenBaseChunks.add(chunkPos);
               }
            }
         } catch (Exception var8) {
         }
      }

   }

   private int extractEntityId(class_2604 packet) {
      try {
         return packet.method_11167();
      } catch (Exception var3) {
         return -1;
      }
   }

   private void applyEntitySpawnAntiEsp(class_2604 spawnPacket) {
      double x = spawnPacket.method_11175();
      double y = spawnPacket.method_11174();
      double z = spawnPacket.method_11176();
      int entityId = this.extractEntityId(spawnPacket);
      if (entityId != -1) {
         class_2338 pos = new class_2338((int)x, (int)y, (int)z);
         class_2680 stateBelow = this.getBlockState(pos.method_10074());
         if (stateBelow != null && (stateBelow.method_27852(class_2246.field_28888) || stateBelow.method_27852(class_2246.field_9987))) {
            this.flagEntityIdAt(entityId, pos);
         }
      }

   }

   private void flagEntityIdAt(int entityId, class_2338 pos) {
      try {
         this.flaggedEntities.add(entityId);
         class_1923 chunkPos = new class_1923(pos);
         this.entityChunks.add(chunkPos);
      } catch (Exception var4) {
      }

   }

   private class_2680 getBlockState(class_2338 pos) {
      return this.mc.field_1687 != null ? this.mc.field_1687.method_8320(pos) : null;
   }

   public static enum RenderMode {
      Pillar,
      Flat;

      // $FF: synthetic method
      private static Finder.RenderMode[] $values() {
         return new Finder.RenderMode[]{Pillar, Flat};
      }
   }
}
