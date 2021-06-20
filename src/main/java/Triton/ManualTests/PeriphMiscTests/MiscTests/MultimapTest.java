package Triton.ManualTests.PeriphMiscTests.MiscTests;

import Triton.Config.Config;
import Triton.ManualTests.TestUtil.TestUtil;
import Triton.ManualTests.TritonTestable;
import com.google.common.collect.HashBasedTable;

public class MultimapTest implements TritonTestable {
    @Override
    public boolean test(Config config) {
        HashBasedTable<String, String, String> test = HashBasedTable.create();
        test.put("a", "b", "haha");

        String o1 = test.get("a", "b");
        String o2 = test.get("b", "a");

        TestUtil.testStringEq("Test if same strings", o1, o2);
        TestUtil.testReferenceEq("Test if same objects", o1, o2);

        TestUtil.enterKeyToContinue();
        return true;
    }
}
