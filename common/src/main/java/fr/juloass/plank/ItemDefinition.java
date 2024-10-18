package fr.juloass.plank;

public class ItemDefinition {

    private final String domain;
    private final String name;
    private final String itemGroup;
    private boolean autoGenerate = false;
    private final ItemType itemType; // New field for ItemType

    private ItemDefinition(Builder builder) {
        this.domain = builder.domain;
        this.name = builder.name;
        this.itemGroup = builder.itemGroup;
        this.autoGenerate = builder.autoGenerate;
        this.itemType = builder.itemType; // New assignment
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public String getItemGroup() {
        return itemGroup;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public ItemType getItemType() { // New getter for ItemType
        return itemType;
    }

    public static class Builder {
        private String domain;
        private String name;
        private String itemGroup;
        private boolean autoGenerate;
        private ItemType itemType; // New field in Builder

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setItemGroup(String itemGroup) {
            this.itemGroup = itemGroup;
            return this;
        }

        public Builder setAutoGenerate(boolean autoGenerate) {
            this.autoGenerate = autoGenerate;
            return this;
        }

        public Builder setItemType(ItemType itemType) { // New setter for ItemType
            this.itemType = itemType;
            return this;
        }

        public ItemDefinition build() {
            return new ItemDefinition(this);
        }
    }
}
