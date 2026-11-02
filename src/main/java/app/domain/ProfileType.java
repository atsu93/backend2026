package app.domain;

public enum ProfileType{
    KIDS{
        @Override
        public boolean canWatch(String ageRating, boolean isPremium){
            if (isPremium) return false;
            return !"R".equalsIgnoreCase(ageRating);
        }
    },

    ADULT{
        @Override
        public boolean canWatch(String ageRating, boolean isPremium){
            return true;
        }
    };

    public abstract boolean canWatch(String ageRating, boolean isPremium);
}
