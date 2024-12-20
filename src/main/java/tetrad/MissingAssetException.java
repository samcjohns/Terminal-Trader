package tetrad;

public class MissingAssetException extends RuntimeException {
    public MissingAssetException(String assetName) {
        super("Missing Asset File: " + assetName);
    }
}