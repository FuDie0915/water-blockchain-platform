package com.water.platform.chain.call;

import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.*;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class KeyValueStoreRawCall extends Contract {

    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061060a806100606000396000f3fe608060405234801561001057600080fd5b5060043610610069576000357c0100000000000000000000000000000000000000000000000000000000900480638da5cb5b1461006e578063a7e21865146100b8578063ccbd3fe81461013b578063d142e4a0146101e2575b600080fd5b610076610289565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b610139600480360360408110156100ce57600080fd5b8101908080359060200190929190803590602001906401000000008111156100f557600080fd5b82018360208201111561010757600080fd5b8035906020019184600183028401116401000000008311171561012957600080fd5b90919293919293905050506102ae565b005b6101676004803603602081101561015157600080fd5b81019080803590602001909291905050506103d4565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156101a757808201518184015260208101905061018c565b50505050905090810190601f1680156101d45780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b61020e600480360360208110156101f857600080fd5b8101908080359060200190929190505050610484565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561024e578082015181840152602081019050610233565b50505050905090810190601f16801561027b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600082829050111515610329576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260168152602001807f537472696e672063616e6e6f7420626520656d7074790000000000000000000081525060200191505060405180910390fd5b818160016000868152602001908152602001600020919061034b929190610539565b50823373ffffffffffffffffffffffffffffffffffffffff167f4e5915ab783fe6cc2a92b4d772460253622404fc706b2a236639260727cc3d9a84844260405180806020018381526020018281038252858582818152602001925080828437600081840152601f19601f82011690508083019250505094505050505060405180910390a3505050565b60016020528060005260406000206000915090508054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561047c5780601f106104515761010080835404028352916020019161047c565b820191906000526020600020905b81548152906001019060200180831161045f57829003601f168201915b505050505081565b6060600160008381526020019081526020016000208054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561052d5780601f106105025761010080835404028352916020019161052d565b820191906000526020600020905b81548152906001019060200180831161051057829003601f168201915b50505050509050919050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061057a57803560ff19168380011785556105a8565b828001600101855582156105a8579182015b828111156105a757823582559160200191906001019061058c565b5b5090506105b591906105b9565b5090565b6105db91905b808211156105d75760008160009055506001016105bf565b5090565b9056fea165627a7a72305820287eda1ce15a595a35deb9f4e1f2ad2d760af755e1109a4cb0d90203fbef46010029"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_key\",\"type\":\"bytes32\"},{\"name\":\"_str\",\"type\":\"string\"}],\"name\":\"bindString\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"keyToString\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_key\",\"type\":\"bytes32\"}],\"name\":\"getStringByKey\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"binder\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"key\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"content\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"bindTime\",\"type\":\"uint256\"}],\"name\":\"StringBinded\",\"type\":\"event\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_BINDSTRING = "bindString";
    public static final String FUNC_GETSTRINGBYKEY = "getStringByKey";

    public static final Event STRINGBINDED_EVENT = new Event("StringBinded",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Bytes32>(true) {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Uint256>() {}));

    protected KeyValueStoreRawCall(String contractAddress, Client client, CryptoKeyPair credential) {
        super(BINARY, contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return BINARY;
    }

    public TransactionReceipt bindString(byte[] _key, String _str) {
        final Function function = new Function(
                FUNC_BINDSTRING,
                Arrays.<Type>asList(
                        new Bytes32(_key),
                        new Utf8String(_str)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public String getStringByKey(byte[] _key) throws org.fisco.bcos.sdk.transaction.model.exception.ContractException {
        final Function function = new Function(
                FUNC_GETSTRINGBYKEY,
                Arrays.<Type>asList(new Bytes32(_key)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public static KeyValueStoreRawCall load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new KeyValueStoreRawCall(contractAddress, client, credential);
    }
}
