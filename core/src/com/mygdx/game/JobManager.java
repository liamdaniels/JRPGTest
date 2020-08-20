package com.mygdx.game;

public class JobManager {

    public static class Job{
        public String name;
        public int strength;
        public int chunk;
        public int sharp;
        public int hard;
        public int speed;
        public int vital;
        public int bigB;

        public int abil1;
        public int abil2;
        public int abil3;
        public int abil4;
        public int abil5;


        public Job(String name, int s, int c, int sh, int h, int sp, int v, int b,
                   int abil1, int abil2, int abil3, int abil4, int abil5){
            this.name = name;
            strength = s;
            chunk = c;
            sharp = sh;
            hard = h;
            speed = sp;
            vital = v;
            bigB = b;
            this.abil1 = abil1;
            this.abil2 = abil2;
            this.abil3 = abil3;
            this.abil4 = abil4;
            this.abil5 = abil5;
        }

    }


    public static Job[] jobs = {
            new Job("Normal",0, 0, 0, 0, 0, 0, 0,
                    -1, -1, -1, -1, -1),
            new Job("Lawyer", 5,5,20,-30,0,10, 10,
                    0,1,2,3,3)
    };




}
