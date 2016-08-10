package com.sousoum.jcvd;

import junit.framework.TestCase;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.sousoum.jcvd.matchers.StorableLocationFenceMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableLocationFenceTest extends TestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testValues() {
        StorableLocationFence fence = StorableLocationFence.entering(1.0, 2.0, 3.0);
        assertThat(fence, is(StorableLocationFence.ENTER_TYPE, 1.0, 2.0, 3.0, 0));

        fence = StorableLocationFence.exiting(1.0, 2.0, 3.0);
        assertThat(fence, is(StorableLocationFence.EXIT_TYPE, 1.0, 2.0, 3.0, 0));

        fence = StorableLocationFence.in(1.0, 2.0, 3.0, 10);
        assertThat(fence, is(StorableLocationFence.IN_TYPE, 1.0, 2.0, 3.0, 10));
    }

    @Test
    public void testEquals() {
        StorableLocationFence fence1 = StorableLocationFence.entering(1.0, 2.0, 3.0);
        StorableLocationFence fence2 = StorableLocationFence.exiting(1.0, 2.0, 3.0);
        StorableLocationFence fence3 = StorableLocationFence.in(1.0, 2.0, 3.0, 10);
        StorableLocationFence fence4 = StorableLocationFence.in(1.0, 2.0, 3.0, 10);
        StorableLocationFence fence5 = StorableLocationFence.in(2.0, 2.0, 3.0, 10);

        assertThat(fence1.equals(fence1), Matchers.is(true));
        assertThat(fence3.equals(fence4), Matchers.is(true));
        assertThat(fence2.equals(null), Matchers.is(false));
        assertThat(fence4.equals(fence5), Matchers.is(false));
    }
}