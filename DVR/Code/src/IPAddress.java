import java.io.Serializable;
import java.util.Arrays;

//Done!
public class IPAddress implements Serializable {
    private Short bytes[];
    private String string;

    public IPAddress(String string) {
        bytes = new Short[4];
        this.string = string;
        String[] temp = string.split("\\.");
        for (int i = 0; i < 4; i++) {
            bytes[i] = Short.parseShort(temp[i]);
        }
    }

    public Short[] getBytes()
    {
        return bytes;
    }

    public String getString()
    {
        return string;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof IPAddress)) {
            return false;
        }

        IPAddress ip = (IPAddress) o;

        return this.string.equals(ip.getString());
    }

    public int hashCode() {
        return Arrays.deepHashCode(this.bytes);
    }

    @Override
    public String toString() { return string; }
}
