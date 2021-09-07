package bftsmart.demo.abci;

import bftsmart.demo.map.MapRequestType;
import bftsmart.tom.ServiceProxy;
import com.google.protobuf.ByteString;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A simple implementation of a Map client made to work with the current AbciClient connector
 *
 * @author rSimoes
 */
public class AbciStoreClient implements Map<String, String> {

    ServiceProxy serviceProxy;

    public AbciStoreClient(int clientId) { serviceProxy = new ServiceProxy(clientId); }

    public String put(String key, String value) {
            String operation = key + "=" +  value;
            ByteString operationByteString = ByteString.copyFromUtf8(operation);

            byte[] reply = serviceProxy.invokeOrdered(operationByteString.toByteArray());
            //if (reply.length == 0)
            //    return null;
            ByteString replyByteString = ByteString.copyFrom(reply);
            return replyByteString.toStringUtf8();
    }

    @Override
    public String remove(Object o) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void clear() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public Set<String> keySet() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public Collection<String> values() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public Set<Entry<String, String>> entrySet() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public int size() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public boolean isEmpty() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public boolean containsKey(Object o) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public boolean containsValue(Object o) { throw new UnsupportedOperationException("Not supported yet."); }

    @SuppressWarnings("unchecked")
    public String get(Object key) {
            ByteString operationByteString = ByteString.copyFromUtf8((String) key);

            byte[] reply = serviceProxy.invokeUnordered(operationByteString.toByteArray());
            //if (reply.length == 0)
            //    return null;
            ByteString replyByteString = ByteString.copyFrom(reply);
            return replyByteString.toStringUtf8();
    }

    public void close() {
        serviceProxy.close();
    }
}
