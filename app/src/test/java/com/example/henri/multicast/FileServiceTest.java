package com.example.henri.multicast;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.expectPrivate;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
    public void pathOneStepDownTest() throws Exception {
        suppress(constructor(FileService.class));
        final FileService fileService = new FileService("");
        // Test
        System.out.println("Testing FileService.pathOneStepDown...");
        assertEquals("", fileService.pathOneStepDown(File.separator + ""));
        assertEquals("", fileService.pathOneStepDown(File.separator + "step"));
        assertEquals("", fileService.pathOneStepDown(File.separator + "step" + File.separator));
        assertEquals("iam", fileService.pathOneStepDown("iam" + File.separator + "step"));
        assertEquals("iam", fileService.pathOneStepDown("iam" + File.separator + "step/"));
        assertEquals(File.separator + "iam", fileService.pathOneStepDown(File.separator + "iam" + File.separator + "step"));
        assertEquals(File.separator + "iam", fileService.pathOneStepDown(File.separator + "iam" + File.separator + "step" + File.separator));
        System.out.println("FileService.pathOneStepDown - Ok");
    }

    @Rule
    TemporaryFolder dir = new TemporaryFolder();

    @Test
    public void createFileViewTest() throws Exception {
        suppress(constructor(FileService.class));
        final FileService fileService = new FileService("");
        // Create files and folders
        File f1 = dir.newFolder("Dir");
        File f2 = dir.newFile("Dir" + File.separator + "text.txt");
        File f3 = dir.newFile("Dir" + File.separator + "file.txt");
        File f4 = dir.newFolder("Dir", "Dir2");
        // Mock private method of FileService
        FileService fs = PowerMockito.spy(new FileService(""));
        PowerMockito.doReturn(f1.getPath()).when(fs, "getAbsolutePath", "Dir");
        // Create Lists
        ArrayList<String> files = fs.createFileView("Dir");
        ArrayList<String> check = new ArrayList<String>();
        check.add("Dir" + File.separator + "Dir2");
        check.add("Dir" + File.separator + "file.txt");
        check.add("Dir"+File.separator+"text.txt");
        // Test
        System.out.println("Testin FileService.createFileView...");
        assertEquals(check, files);
        // Create new files and folders
        f2 = dir.newFile(File.separator + "text.txt");
        f3 = dir.newFile("file.txt");
        f4 = dir.newFolder("Dir2"+File.separator);
        // Check if works in root dir as well
        PowerMockito.doReturn(fileService.pathOneStepDown(f1.getPath())).when(fs, "getAbsolutePath", "");
        // Create lists
        files = fs.createFileView("");
        check = new ArrayList<String>();
        check.add(File.separator+"Dir");
        check.add(File.separator+"Dir2");
        check.add(File.separator+"file.txt");
        check.add(File.separator+"text.txt");
        // Test
        assertEquals(check, files);
        System.out.println("FileService.createFileView - Ok");
    }
}
