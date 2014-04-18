package com.github.alternet.demo.criminal.client.model;

public enum Aptitude {

    BEST {
        @Override
        public String gradient() {
            return "green, rgba(0,255,0,0)";
        }
    },
    BETTER {
        @Override
        public String gradient() {
            return "rgb(40,220,40), rgba(0,255,0,0)";
        }
    },
    AVERAGE {
        @Override
        public String gradient() {
            return "rgb(200,200,200), rgba(200,200,200,0)";
        }
    },
    BAD {
        @Override
        public String gradient() {
            return "rgb(255,165,0), rgba(255,165,0,0)";
        }
    },
    WORST {
        @Override
        public String gradient() {
            return "red, rgba(255,0,0,0)";
        }
    };

    public abstract String gradient();

}
