package com.study.spring5reactive.chapter9_reactive_test;

import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;

// TODO: 별도 정리
// https://www.lesstif.com/java/hamcrest-junit-test-case-18219426.html
public class HamcrestTest {

    @Test
    public void null_test() {
        String str = null;

        assertThat(str, is(nullValue()));
        assertThat(str, not(notNullValue()));
    }

    @Test
    public void instanceOf_and_sameInstance_test() {
        Map<String, String> map = new HashMap<>();
        Map<String, String> sameMap = map;

        assertThat(map, instanceOf(Map.class));
        assertThat(sameMap, sameInstance(map));
    }

    @Test
    public void numbers_test() {
        assertThat(2, greaterThan(1));
        assertThat(1, greaterThanOrEqualTo(1));

        assertThat(0, lessThan(1));
        assertThat(0, lessThanOrEqualTo(0));
    }

    public class MyUser {
        private String id;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    @Test
    public void bean_test() {
        MyUser myUser = new MyUser();
        myUser.setId("leo");

        // getter or setter 존재시 true
        assertThat(myUser, hasProperty("id"));
        // getter 존재하지 않으면 오류
        assertThat(myUser, hasProperty("id", equalTo("leo")));
    }

    @Test
    public void collection_test() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");

        assertThat(map, IsMapContaining.hasKey("foo1"));
        assertThat(map, IsMapContaining.hasEntry("foo1", "bar1"));
        assertThat(map, IsMapContaining.hasValue("bar1"));
    }

    @Test
    public void text_test() {
        // 대소문자 구분 없음
        assertThat("Spring", equalToIgnoringCase("spring"));
        //
        assertThat("Spring Framework", containsString("Framework"));
    }
}
