package com.example.henri.multicast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 * Created by Jasu on 23.2.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileService.class)
public class ListAdapterTest {

    @Test
    public void getFileNameFromPathTest() throws Exception{
        suppress(constructor(ListAdapter.class));
        final ListAdapter listAdapter = new ListAdapter(null, null);

        Method m = ListAdapter.class.getDeclaredMethod("getFileNameFromPath", String.class);
        m.setAccessible(true);

        System.out.println("Testing ListAdapter.getFileNameFromPath...");

        assertEquals("", (String) m.invoke(listAdapter, ""));
        assertEquals("", (String) m.invoke(listAdapter, File.separator+""));
        assertEquals("step", (String) m.invoke(listAdapter, File.separator+"step"));
        assertEquals("step", (String) m.invoke(listAdapter, File.separator+"step"+File.separator));
        assertEquals("step", (String) m.invoke(listAdapter, "iam"+File.separator+"step"));
        assertEquals("step", (String) m.invoke(listAdapter, "iam" + File.separator + "step"+File.separator));
        assertEquals("step", (String) m.invoke(listAdapter, File.separator+"iam"+File.separator+"step"));
        assertEquals("step", (String) m.invoke(listAdapter, File.separator+"iam"+File.separator+"step"+File.separator));

        System.out.println("Ok");
    }
}
