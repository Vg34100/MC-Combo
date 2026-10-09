# Combo Mod — Game Specification (GAME_SPEC.md)

This document specifies the combat design, mechanics, and feature roadmap for the **Combo** mod (`net.vg.combo`).

---

## 1. Core Combat Systems (Implemented)

### A. Melee Combo System
- **Combo Chain:** Landing timely, sufficiently charged main-hand melee attacks increments the combo stage.
- **Timeout Window:** Combo chains expire after a short idle timeout (12 game ticks / 0.6 seconds).
- **Finisher:** The 3rd confirmed hit in a chain acts as a Finisher:
  - Deals a configurable damage multiplier.
  - Applies stronger knockback and stun/stagger to targets.
  - Triggers distinct visual and audible impact feedback.
  - Custom finisher animations/effects for specific weapon types.
- **HUD Indicator:** Compact client HUD indicator displaying the active combo stage and remaining timeout.

### B. Timed Parry System
- **Shield Timing:** Activating shield block right before an incoming enemy attack lands triggers a successful parry.
- **Effects:** Negates damage, knocks the attacker back, and inflicts a brief stun opening for counter-attacks.
- **Stamina Cost:** Consumes stamina on activation, but costs significantly less than absorbing full blows.

### C. Stealth & Sneak Attack System
- **Detection Reduction:** Crouching/sneaking drastically reduces mob detection radius.
- **Backstab:** Striking unaware enemies from behind inflicts substantial bonus damage.
- **Silent Finisher / Takedown:** Approaching unaware enemies within close melee range allows executing a high-damage silent takedown.

### D. Directional Dodge
- **Movement Dodge:** Quick evasive dash in the direction of player input/movement.
- **Mobility:** Enables repositioning around attacks and evading projectile trajectories.
- **Stamina Cost:** Balanced stamina cost to prevent endless dodge spamming.

### E. Stamina Economy
- A unified stamina bar governing physical combat actions:
  - Dodging
  - Parrying
  - Sprinting
  - Shield blocking
  - Finisher attacks
  - Sustained sneaking
- Actions cannot be performed or suffer severe penalties if the stamina bar is depleted.

---

## 2. Roadmap & Potential V2 Features

### A. Perfect Dodge Window
- Dodging within a tight split-second window before an attack lands executes a "Perfect Dodge".
- Refunds stamina or opens an immediate counter-attack strike opportunity (similar to the parry window).

### B. Armor Weight & Mobility Scaling
- Heavier armor (Netherite, Iron) increases stamina costs and reduces dodge distance.
- Light or unarmored setups grant longer, cheaper dodges and faster stamina regeneration, creating genuine gear-choice trade-offs.

### C. Posture / Break Meter (Tough Mobs & Bosses)
- Distinct from the finisher stagger: sustained hits on elite mobs build up a Posture/Break meter (Sekiro-style).
- Breaking posture forces an extended critical strike opening in open combat.

### D. Limited-Use Tactical Healing
- A rechargeable healing flask (Estus-style item) with limited charges.
- Refills at beds, campfires, or specific utility blocks.

### E. Continuous Combo Counter & Streak Takedowns
- **Combo Counter:** Tracking an ongoing hit counter; reaching higher thresholds boosts attack speed, movement, and stamina recovery.
- **Streak Finisher:** Sustaining high combos unlocks a temporary AoE or multi-target execute window.

### F. Hit-Stop Impact Effects
- Brief 1-2 tick freeze-frame hitch / micro-slowdown on finisher and takedown impacts to give combat tactile weight and impact.
