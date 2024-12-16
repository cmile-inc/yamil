/*
 * Copyright 2024 cmile inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmile.serviceutil.common;

import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgMetricRegistryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {CfgMetricRegistryTest.class})
class JsonEntityMapperTest extends AbstractCommonTest {


    @BeforeEach
    public void setUp() {

    }

    @Test
    void testReadJsonToEntity_Success() throws IOException {
        String json = "{\"name\":\"John Doe\"}";
        Class<Person> entityClass = Person.class;
        Person expectedPerson = new Person("John Doe");
        Person actualPerson = jsonEntityMapper.readJsonToEntity(json, entityClass);
        assertEquals(expectedPerson, actualPerson);
    }

    @Test
    void testReadJsonToEntity_Failure() throws IOException {
        String json = "{invalidJson}";
        Class<Person> entityClass = Person.class;
        assertThrows(Exception.class, () -> {
            jsonEntityMapper.readJsonToEntity(json, entityClass);
        });

    }

    @Test
    void testWriteEntityToJson_Success() throws IOException {
        Person person = new Person("John Doe");
        String expectedJson = "{\"name\":\"John Doe\"}";
        String actualJson = jsonEntityMapper.writeEntityToJson(person);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testWriteEntityToJson_Failure() throws IOException {
        DateSample dateSample = new DateSample(Instant.now());
        assertThrows(IOException.class, () -> {
            jsonEntityMapper.writeEntityToJson(dateSample);
        });

    }

    static class DateSample {
        private Instant dateTime;

        public DateSample(Instant now) {
            this.dateTime = now;
        }
    }

    // Sample Person class for testing purposes
    static class Person {
        private String name;

        public Person() {
        }

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Person person = (Person) obj;
            return name.equals(person.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
