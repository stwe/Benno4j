package de.sg.benno.chunk;

import de.sg.benno.BennoRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class Chunk {

    private static final int ID_SIZE_IN_BYTES = 16;
    private static final int DATA_LENGTH_SIZE_IN_BYTES = 4;

    /**
     * The Chunk type identifier.
     */
    private final byte[] id = new byte[ID_SIZE_IN_BYTES];

    /**
     * The Chunk type identifier as String.
     */
    private String idStr;

    /**
     * The size of the Chunk, excluding the header.
     */
    private final byte[] dataLength = new byte[DATA_LENGTH_SIZE_IN_BYTES];

    /**
     * The size of the Chunk, excluding the header as Int.
     */
    private int dataLengthInt;

    /**
     * The ByteBuffer with the Chunk data.
     */
    private ByteBuffer byteBuffer;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public Chunk(InputStream inputStream) throws IOException {
        readId(inputStream);
        readDataLength(inputStream);
        readData(inputStream);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public String getId() {
        return idStr;
    }

    public int getDataLength() {
        return dataLengthInt;
    }

    public ByteBuffer getData() {
        return byteBuffer;
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void readId(InputStream inputStream) throws IOException {
        var result = Objects.requireNonNull(inputStream, "inputStream must not be null").read(id, 0, id.length);
        checkNumberOfBytesRead(result, id.length);

        idStr = new String(id).split("\0")[0];
    }

    private void readDataLength(InputStream inputStream) throws IOException {
        var result = Objects.requireNonNull(inputStream, "inputStream must not be null").read(dataLength, 0, dataLength.length);
        checkNumberOfBytesRead(result, dataLength.length);

        dataLengthInt = ByteBuffer.wrap(dataLength).order(ByteOrder.nativeOrder()).getInt();
    }

    private void readData(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[dataLengthInt];

        var offset = 0;
        while (offset < bytes.length) {
            var result = Objects.requireNonNull(inputStream, "inputStream must not be null").read(bytes, offset, dataLengthInt - offset);
            if (result == -1) {
                break;
            }
            offset += result;
        }

        checkNumberOfBytesRead(offset, dataLengthInt);

        byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
    }

    private void checkNumberOfBytesRead(int result, int length) {
        if (result != length) {
            if (idStr != null) {
                throw new BennoRuntimeException("Wrong total number of bytes read. Id: " + idStr + ", result: " + result + ", expected length: " + length);
            }

            throw new BennoRuntimeException("Wrong total number of bytes read. result: " + result + ", expected length: " + length);
        }
    }
}
