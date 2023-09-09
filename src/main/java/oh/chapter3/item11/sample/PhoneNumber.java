package oh.chapter3.item11.sample;

import java.util.Objects;

public final class PhoneNumber {
    private int areaCode;
    private int prefix;
    private int lineNum;

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PhoneNumber))
            return false;
        PhoneNumber pn = (PhoneNumber) obj;
        return pn.lineNum == lineNum && pn.areaCode == areaCode && pn.prefix == prefix;
    }

}
