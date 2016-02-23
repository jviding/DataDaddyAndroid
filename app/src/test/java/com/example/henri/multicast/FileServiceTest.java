package com.example.henri.multicast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import org.junit.Before;
import java.io.File;
import static org.mockito.Matchers.anyString;

/**
 * Created by Jasu on 23.2.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileService.class)
public class FileServiceTest {

    @Before
    public void setUp() {
    }

    @Test
    public void pathOneStepDownTest() throws Exception{
        suppress(constructor(FileService.class));
        final FileService fileService = new FileService(anyString());
        assertEquals("", fileService.pathOneStepDown(File.separator+""));
        assertEquals("", fileService.pathOneStepDown(File.separator+"step"));
        assertEquals("", fileService.pathOneStepDown(File.separator+"step"+File.separator));
        assertEquals("iam", fileService.pathOneStepDown("iam"+File.separator+"step"));
        assertEquals("iam", fileService.pathOneStepDown("iam" + File.separator + "step/"));
        assertEquals(File.separator+"iam", fileService.pathOneStepDown(File.separator+"iam"+File.separator+"step"));
        assertEquals(File.separator+"iam", fileService.pathOneStepDown(File.separator+"iam"+File.separator+"step"+File.separator));
    }
}
