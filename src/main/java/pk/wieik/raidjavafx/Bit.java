package pk.wieik.raidjavafx;

public class Bit {
    private boolean bit;
    private boolean isParityBit;

    public Bit(boolean bit) {
        this.bit = bit;
    }

    public boolean getBit() {
        return bit;
    }

    public void setBit(boolean bit) {
        this.bit = bit;
    }

    public boolean isParityBit() {
        return isParityBit;
    }

    public void setParityBit(boolean isParityBit) {
        this.isParityBit = isParityBit;
    }
}

