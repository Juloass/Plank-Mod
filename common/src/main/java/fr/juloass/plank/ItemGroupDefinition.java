package fr.juloass.plank;

import net.minecraft.resources.ResourceLocation;

public class ItemGroupDefinition {

    private final String domain;
    private final String name;

    private ItemGroupDefinition(Builder builder) {
        this.domain = builder.domain;
        this.name = builder.name;
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public static class Builder {
        private String domain;
        private String name;

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public ItemGroupDefinition build() {
            return new ItemGroupDefinition(this);
        }
    }
}
