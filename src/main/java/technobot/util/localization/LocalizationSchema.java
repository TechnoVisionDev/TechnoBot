package technobot.util.localization;

import technobot.util.localization.SchemaUtils.Result;
import technobot.util.localization.SchemaUtils.Value;

public record LocalizationSchema(
        Automation automation,
        Economy economy,
        Fun fun,
        Greeting greeting,
        Levels levels
) {
    public record Automation(AutoRole autoRole) {
        public record AutoRole(Add add, Result remove, List list) {
            public record Add(String higherLevel, String premium, String maxRolesReached, String roleAdded) {
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
            String cute,
            EightBall eightBall,
            Emote emote,
            String google,
            String joke,
            String meme,
            String nsfw,
            String reddit,
            String surprise
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
    }

    public record Greeting(Value farewell, Value greet, Greetings greetings, Value joinDm) {
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
    }

    public record Levels(Leveling leveling, RankCard rankCard, Rank rank, Rewards rewards, Top top) {
        public record Leveling(
                Channel channel,
                Value message,
                Dm dm,
                Value mod,
                ServerBackground serverBackground,
                Mute mute,
                Reward reward,
                Config config,
                String reset,
                String resetAll
        ) {
            public record Channel(String specific, String user) {
            }

            public record Dm(String enable, String disable) {
            }

            public record ServerBackground(String set, String reset, String failure) {
            }

            public record Mute(String enable, String disable) {
            }

            public record Reward(String add, String remove, String failure) {
            }

            public record Config(
                    String channel,
                    String modulus,
                    String muted,
                    String dms,
                    String message,
                    String background
            ) {
            }
        }

        public record RankCard(
                String noRank,
                Result background,
                Result color,
                Result accent,
                String opacity,
                String reset
        ) {
        }

        public record Rank(String noRankSelf, String noRankOther, String accessFailure) {
        }

        public record Rewards(String reward, String title, String noRewards) {
        }

        public record Top(Guild guild, Leveling leveling, Economy economy, String footer) {
            public record Guild(String name, String more, String title) {
            }

            public record Leveling(String name, String empty, String entry) {
            }

            public record Economy(String name, String empty, String entry) {
            }
        }
    }
}
