package com.mygdx.game;

public class DamageCalculator {


    public static int calcAttackDamage(Battleable attacker, Battleable defender){
        int attack = attacker.getAttack();
        int defense = defender.getDefense();
        double roll = (Math.random() * 0.5f) + 0.75f;
        return (int)(((attack*attack) / (2.0f*defense))*roll) + 1;
    }

    public static int calcMagicDamage(BattlePlayer player, Battleable enemy, int power, int element){
        int attack = player.getMagicAttack();
        int defense = enemy.getMagicDefense();
        double roll = (Math.random() * 0.3f) + 0.85f;

        int damage = (int)(((attack*power*2) / (defense))*roll) + 1;

        if (enemy.weakTo(element)){
            damage *= 2;
        }
        return damage;
    }

    /**
     * Calc healing
     * @param magicAttack   Caster's magic attack
     * @param magicDefense  Caster's magic defense
     * @param power         Power of healing spell
     * @return              Total HP healed
     */
    public static int calcMagicHeal(int magicAttack, int magicDefense, int power){
        double roll = (Math.random() * 0.3f) + 0.85f;
        double healPower = Math.sqrt((magicAttack*0.9f) + (magicDefense*0.1f));
        return (int)(((healPower*power)/3)*roll) + 1;
    }

    public static int calcEnemyDamage(int attack, int defense, int power){
        double roll = (Math.random() * 0.5f) + 0.75f;
        return (int)(((attack*power*1.5) / (defense))*roll) + 1;
    }


}
