package technobot.util.localization;

public record LocalizationSchema(
        Automation automation,
        Economy economy,
        Fun fun,
        Greeting greeting
) {
    public record Automation(AutoRole autoRole) {
        public record AutoRole(Add add, Remove remove, List list) {
            public record Add(String higherLevel, String premium, String maxRolesReached, String roleAdded) {
            }

            public record Remove(String roleNotSet, String roleRemoved) {
            }

            public record List(String noAutoRoles, String premium, String roleCount, String role) {
            }

        }
    }

    /**
     * Represents list of responses to economy commands.
     *
     * @author TechnoVision
     */
    public record Economy(
            String replyId,
            Balance balance,
            Crime crime,
            Deposit deposit,
            Pay pay,
            Rob rob,
            Withdraw withdraw,
            Work work
    ) {
        public record Balance(String leaderboardRank, String cash, String bank, String total) {
        }

        public record Crime(String timeout, String[] success, String[] failure) {
        }

        public record Deposit(String noMoney, String notEnough, String success) {
        }

        public record Pay(String paySelf, String payBots, String notEnough, String success) {
        }

        public record Rob(String robSelf, String robBots, String timeout, String failure, String success) {
        }

        public record Withdraw(String noMoney, String tooMuch, String success) {
        }

        public record Work(String timeout, String[] success) {
        }
    }

    public record Fun(
            Action action,
            Cute cute,
            EightBall eightBall,
            Emote emote,
            Google google,
            Joke joke,
            Meme meme,
            Nsfw nsfw,
            Reddit reddit,
            Surprise surprise
    ) {
        public record Action(
                String failure,
                String bite,
                String brofist,
                String cuddle,
                String handhold,
                String hug,
                String kiss,
                String lick,
                String pat,
                String pinch,
                String poke,
                String punch,
                String slap,
                String smack,
                String sorry,
                String stare,
                String thumbsup,
                String tickle,
                String wave,
                String wink
        ) {
        }

        public record Cute(String failure) {
        }

        public record EightBall(String tooLong, String[] responses) {
        }

        public record Emote(
                String failure,
                String mad,
                String blush,
                String celebrate,
                String clap,
                String confused,
                String cry,
                String dance,
                String facepalm,
                String happy,
                String laugh,
                String pout,
                String shrug,
                String shy,
                String sigh,
                String slowClap,
                String scared,
                String sleep,
                String yawn
        ) {
        }

        public record Google(String tooLong) {
        }

        public record Joke(String failure) {
        }

        public record Meme(String failure) {
        }

        public record Nsfw(String failure) {
        }

        public record Reddit(String failure) {
        }

        public record Surprise(String failure) {
        }
    }

    public record Greeting(Farewell farewell, Greet greet, Greetings greetings, JoinDM joinDm) {
        public record Farewell(String set, String removed) {
        }

        public record Greet(String set, String removed) {
        }

        public record Greetings(
                String removed,
                String set,
                String reset,
                String welcomeConfig,
                String greetingConfig,
                String farewellConfig,
                String joinDmConfig
        ) {
        }

        public record JoinDM(String set, String removed) {
        }
    }
}
