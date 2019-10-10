package com.smartbear.readyapi4j.testengine.teststeps.datasource.datagen;

import com.smartbear.readyapi4j.client.model.DataGenerator;
import com.smartbear.readyapi4j.client.model.NameDataGenerator;

public class NameDataGeneratorBuilder extends AbstractDataGeneratorBuilder<NameDataGeneratorBuilder> {
    private final NameDataGenerator nameDataGenerator = new NameDataGenerator();

    NameDataGeneratorBuilder(String property) {
        super(property);
        nameDataGenerator.setType("Name");
        nameDataGenerator.setGender(NameDataGenerator.GenderEnum.ANY);
        nameDataGenerator.setNameType(NameDataGenerator.NameTypeEnum.FULL);
    }

    NameDataGeneratorBuilder withGenderMale() {
        nameDataGenerator.setGender(NameDataGenerator.GenderEnum.MALE);
        return this;
    }

    NameDataGeneratorBuilder withGenderFemale() {
        nameDataGenerator.setGender(NameDataGenerator.GenderEnum.FEMALE);
        return this;
    }

    NameDataGeneratorBuilder withFirstNames() {
        nameDataGenerator.setNameType(NameDataGenerator.NameTypeEnum.FIRSTNAME);
        return this;
    }

    NameDataGeneratorBuilder withLatsNames() {
        nameDataGenerator.setNameType(NameDataGenerator.NameTypeEnum.LASTNAME);
        return this;
    }

    @Override
    protected DataGenerator buildDataGenerator() {
        return nameDataGenerator;
    }
}
