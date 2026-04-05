package com.water.platform.chain.call;

import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.*;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class WaterRawCall extends Contract {
    public static final String[] BINARY_ARRAY = {"60806040523480156200001157600080fd5b50604051604080620015918339810180604052620000339190810190620000d2565b816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505062000147565b6000620000ca825162000133565b905092915050565b60008060408385031215620000e657600080fd5b6000620000f685828601620000bc565b92505060206200010985828601620000bc565b9150509250929050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000620001408262000113565b9050919050565b61143a80620001576000396000f3fe608060405234801561001057600080fd5b50600436106100a5576000357c0100000000000000000000000000000000000000000000000000000000900480637a08abd9116100785780637a08abd914610137578063868b96e5146101675780638bf8503a146101835780639d783219146101b3576100a5565b806308df4e7d146100aa578063481c6a75146100df57806364c341e7146100fd5780636904c94d14610119575b600080fd5b6100c460048036036100bf9190810190610d2f565b6101e6565b6040516100d69695949392919061124a565b60405180910390f35b6100e761036e565b6040516100f4919061111f565b60405180910390f35b61011760048036036101129190810190610de6565b610394565b005b6101216104b4565b60405161012e919061111f565b60405180910390f35b610151600480360361014c9190810190610d2f565b6104d9565b60405161015e919061115c565b60405180910390f35b610181600480360361017c9190810190610d6b565b6107cf565b005b61019d60048036036101989190810190610d06565b610949565b6040516101aa919061113a565b60405180910390f35b6101cd60048036036101c89190810190610d2f565b610a7c565b6040516101dd94939291906111c7565b60405180910390f35b60026020528160005260406000208181548110151561020157fe5b906000526020600020906006020160009150915050806000015490806001015490806002015490806003018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102c05780601f10610295576101008083540402835291602001916102c0565b820191906000526020600020905b8154815290600101906020018083116102a357829003601f168201915b505050505090806004018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561035e5780601f106103335761010080835404028352916020019161035e565b820191906000526020600020905b81548152906001019060200180831161034157829003601f168201915b5050505050908060050154905086565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b61039c610b60565b60c0604051908101604052808881526020018781526020018681526020018581526020018481526020018381525090506002600087815260200190815260200160002081908060018154018082558091505090600182039060005260206000209060060201600090919290919091506000820151816000015560208201518160010155604082015181600201556060820151816003019080519060200190610445929190610b97565b506080820151816004019080519060200190610462929190610b97565b5060a082015181600501555050507f3fcb7509b31066be1cd71c724906bcf84b83ee08e09356b4a97a12382355829e8787876040516104a393929190611213565b60405180910390a150505050505050565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60608060026000858152602001908152602001600020805480602002602001604051908101604052809291908181526020016000905b828210156106ae578382906000526020600020906006020160c06040519081016040529081600082015481526020016001820154815260200160028201548152602001600382018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105ea5780601f106105bf576101008083540402835291602001916105ea565b820191906000526020600020905b8154815290600101906020018083116105cd57829003601f168201915b50505050508152602001600482018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561068c5780601f106106615761010080835404028352916020019161068c565b820191906000526020600020905b81548152906001019060200180831161066f57829003601f168201915b505050505081526020016005820154815250508152602001906001019061050f565b505050509050600080905060008090505b8251811015610701578483828151811015156106d757fe5b906020019060200201516040015114156106f45781806001019250505b80806001019150506106bf565b5060608160405190808252806020026020018201604052801561073e57816020015b61072b610c17565b8152602001906001900390816107235790505b509050600080905060008090505b84518110156107c15786858281518110151561076457fe5b906020019060200201516040015114156107b457848181518110151561078657fe5b90602001906020020151838381518110151561079e57fe5b9060200190602002018190525081806001019250505b808060010191505061074c565b508194505050505092915050565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610860576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016108579061117e565b60405180910390fd5b610868610c4e565b608060405190810160405280868152602001858152602001848152602001838152509050600360008581526020019081526020016000208190806001815401808255809150509060018203906000526020600020906004020160009091929091909150600082015181600001556020820151816001015560408201518160020190805190602001906108fb929190610b97565b50606082015181600301555050507ff390ee506b2ee55942afd4e04d7e77bbc996cc1f21ba801f2eb143b8e91f36d2858560405161093a92919061119e565b60405180910390a15050505050565b606060036000838152602001908152602001600020805480602002602001604051908101604052809291908181526020016000905b82821015610a715783829060005260206000209060040201608060405190810160405290816000820154815260200160018201548152602001600282018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610a4f5780601f10610a2457610100808354040283529160200191610a4f565b820191906000526020600020905b815481529060010190602001808311610a3257829003601f168201915b505050505081526020016003820154815250508152602001906001019061097e565b505050509050919050565b600360205281600052604060002081815481101515610a9757fe5b906000526020600020906004020160009150915050806000015490806001015490806002018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610b505780601f10610b2557610100808354040283529160200191610b50565b820191906000526020600020905b815481529060010190602001808311610b3357829003601f168201915b5050505050908060030154905084565b60c0604051908101604052806000815260200160008152602001600081526020016060815260200160608152602001600081525090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610bd857805160ff1916838001178555610c06565b82800160010185558215610c06579182015b82811115610c05578251825591602001919060010190610bea565b5b509050610c139190610c77565b5090565b60c0604051908101604052806000815260200160008152602001600081526020016060815260200160608152602001600081525090565b608060405190810160405280600081526020016000815260200160608152602001600081525090565b610c9991905b80821115610c95576000816000905550600101610c7d565b5090565b90565b600082601f8301121515610caf57600080fd5b8135610cc2610cbd826112e6565b6112b9565b91508082526020830160208301858383011115610cde57600080fd5b610ce98382846113ad565b50505092915050565b6000610cfe82356113a3565b905092915050565b600060208284031215610d1857600080fd5b6000610d2684828501610cf2565b91505092915050565b60008060408385031215610d4257600080fd5b6000610d5085828601610cf2565b9250506020610d6185828601610cf2565b9150509250929050565b60008060008060808587031215610d8157600080fd5b6000610d8f87828801610cf2565b9450506020610da087828801610cf2565b935050604085013567ffffffffffffffff811115610dbd57600080fd5b610dc987828801610c9c565b9250506060610dda87828801610cf2565b91505092959194509250565b60008060008060008060c08789031215610dff57600080fd5b6000610e0d89828a01610cf2565b9650506020610e1e89828a01610cf2565b9550506040610e2f89828a01610cf2565b945050606087013567ffffffffffffffff811115610e4c57600080fd5b610e5889828a01610c9c565b935050608087013567ffffffffffffffff811115610e7557600080fd5b610e8189828a01610c9c565b92505060a0610e9289828a01610cf2565b9150509295509295509295565b610ea881611367565b","82525050565b6000610eb98261132c565b80845260208401935083602082028501610ed285611312565b60005b84811015610f0b578383038852610eed83835161101d565b9250610ef88261134d565b9150602088019750600181019050610ed5565b508196508694505050505092915050565b6000610f2782611337565b80845260208401935083602082028501610f408561131f565b60005b84811015610f79578383038852610f5b838351611080565b9250610f668261135a565b9150602088019750600181019050610f43565b508196508694505050505092915050565b6000610f9582611342565b808452610fa98160208601602086016113bc565b610fb2816113ef565b602085010191505092915050565b6000602182527fe58faae69c89e4bc81e4b89ae58fafe4bba5e689a7e8a18ce6ada4e6938de4bd60208301527f9c000000000000000000000000000000000000000000000000000000000000006040830152606082019050919050565b60006080830160008301516110356000860182611110565b5060208301516110486020860182611110565b50604083015184820360408601526110608282610f8a565b91505060608301516110756060860182611110565b508091505092915050565b600060c0830160008301516110986000860182611110565b5060208301516110ab6020860182611110565b5060408301516110be6040860182611110565b50606083015184820360608601526110d68282610f8a565b915050608083015184820360808601526110f08282610f8a565b91505060a083015161110560a0860182611110565b508091505092915050565b61111981611399565b82525050565b60006020820190506111346000830184610e9f565b92915050565b600060208201905081810360008301526111548184610eae565b905092915050565b600060208201905081810360008301526111768184610f1c565b905092915050565b6000602082019050818103600083015261119781610fc0565b9050919050565b60006040820190506111b36000830185611110565b6111c06020830184611110565b9392505050565b60006080820190506111dc6000830187611110565b6111e96020830186611110565b81810360408301526111fb8185610f8a565b905061120a6060830184611110565b95945050505050565b60006060820190506112286000830186611110565b6112356020830185611110565b6112426040830184611110565b949350505050565b600060c08201905061125f6000830189611110565b61126c6020830188611110565b6112796040830187611110565b818103606083015261128b8186610f8a565b9050818103608083015261129f8185610f8a565b90506112ae60a0830184611110565b979650505050505050565b6000604051905081810181811067ffffffffffffffff821117156112dc57600080fd5b8060405250919050565b600067ffffffffffffffff8211156112fd57600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b6000602082019050919050565b600061137282611379565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b6000819050919050565b82818337600083830152505050565b60005b838110156113da5780820151818401526020810190506113bf565b838111156113e9576000848401525b50505050565b6000601f19601f830116905091905056fea265627a7a72305820ca6cb5d7633b9dd5aa4a53e902dbd4875223f105a68b3e772c7108b2377e90886c6578706572696d656e74616cf50037"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"waterDataByUserId\",\"outputs\":[{\"name\":\"id\",\"type\":\"uint256\"},{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"dataType\",\"type\":\"uint256\"},{\"name\":\"data\",\"type\":\"string\"},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"time\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"manager\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"id\",\"type\":\"uint256\"},{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"dataType\",\"type\":\"uint256\"},{\"name\":\"data\",\"type\":\"string\"},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"time\",\"type\":\"uint256\"}],\"name\":\"insertWaterData\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"company\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"dataType\",\"type\":\"uint256\"}],\"name\":\"getWaterDataByUserIdAndType\",\"outputs\":[{\"components\":[{\"name\":\"id\",\"type\":\"uint256\"},{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"dataType\",\"type\":\"uint256\"},{\"name\":\"data\",\"type\":\"string\"},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"time\",\"type\":\"uint256\"}],\"name\":\"\",\"type\":\"tuple[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"id\",\"type\":\"uint256\"},{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"imageUrl\",\"type\":\"string\"},{\"name\":\"createTime\",\"type\":\"uint256\"}],\"name\":\"uploadCompanyCert\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"userId\",\"type\":\"uint256\"}],\"name\":\"getCompanyCertByUserId\",\"outputs\":[{\"components\":[{\"name\":\"id\",\"type\":\"uint256\"},{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"imageUrl\",\"type\":\"string\"},{\"name\":\"createTime\",\"type\":\"uint256\"}],\"name\":\"\",\"type\":\"tuple[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"companyCertByUserId\",\"outputs\":[{\"name\":\"id\",\"type\":\"uint256\"},{\"name\":\"userId\",\"type\":\"uint256\"},{\"name\":\"imageUrl\",\"type\":\"string\"},{\"name\":\"createTime\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"companyAddress\",\"type\":\"address\"},{\"name\":\"managerAddress\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"id\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"userId\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"dataType\",\"type\":\"uint256\"}],\"name\":\"WaterDataInserted\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"id\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"userId\",\"type\":\"uint256\"}],\"name\":\"CompanyCertUploaded\",\"type\":\"event\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_WATERDATABYUSERID = "waterDataByUserId";

    public static final String FUNC_MANAGER = "manager";

    public static final String FUNC_INSERTWATERDATA = "insertWaterData";

    public static final String FUNC_COMPANY = "company";

    public static final String FUNC_GETWATERDATABYUSERIDANDTYPE = "getWaterDataByUserIdAndType";

    public static final String FUNC_UPLOADCOMPANYCERT = "uploadCompanyCert";

    public static final String FUNC_GETCOMPANYCERTBYUSERID = "getCompanyCertByUserId";

    public static final String FUNC_COMPANYCERTBYUSERID = "companyCertByUserId";

    public static final Event WATERDATAINSERTED_EVENT = new Event("WaterDataInserted",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event COMPANYCERTUPLOADED_EVENT = new Event("CompanyCertUploaded",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    protected WaterRawCall(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public Tuple6<BigInteger, BigInteger, BigInteger, String, String, BigInteger> waterDataByUserId(BigInteger param0, BigInteger param1) throws ContractException {
        final Function function = new Function(FUNC_WATERDATABYUSERID,
                Arrays.<Type>asList(new Uint256(param0),
                new Uint256(param1)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple6<BigInteger, BigInteger, BigInteger, String, String, BigInteger>(
                (BigInteger) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (BigInteger) results.get(2).getValue(),
                (String) results.get(3).getValue(),
                (String) results.get(4).getValue(),
                (BigInteger) results.get(5).getValue());
    }

    public String manager() throws ContractException {
        final Function function = new Function(FUNC_MANAGER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt insertWaterData(BigInteger id, BigInteger userId, BigInteger dataType, String data, String status, BigInteger time) {
        final Function function = new Function(
                FUNC_INSERTWATERDATA,
                Arrays.<Type>asList(new Uint256(id),
                new Uint256(userId),
                new Uint256(dataType),
                new Utf8String(data),
                new Utf8String(status),
                new Uint256(time)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] insertWaterData(BigInteger id, BigInteger userId, BigInteger dataType, String data, String status, BigInteger time, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_INSERTWATERDATA,
                Arrays.<Type>asList(new Uint256(id),
                new Uint256(userId),
                new Uint256(dataType),
                new Utf8String(data),
                new Utf8String(status),
                new Uint256(time)),
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForInsertWaterData(BigInteger id, BigInteger userId, BigInteger dataType, String data, String status, BigInteger time) {
        final Function function = new Function(
                FUNC_INSERTWATERDATA,
                Arrays.<Type>asList(new Uint256(id),
                new Uint256(userId),
                new Uint256(dataType),
                new Utf8String(data),
                new Utf8String(status),
                new Uint256(time)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple6<BigInteger, BigInteger, BigInteger, String, String, BigInteger> getInsertWaterDataInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_INSERTWATERDATA,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple6<BigInteger, BigInteger, BigInteger, String, String, BigInteger>(

                (BigInteger) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (BigInteger) results.get(2).getValue(),
                (String) results.get(3).getValue(),
                (String) results.get(4).getValue(),
                (BigInteger) results.get(5).getValue()
                );
    }

    public String company() throws ContractException {
        final Function function = new Function(FUNC_COMPANY,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public DynamicArray<Struct0> getWaterDataByUserIdAndType(BigInteger userId, BigInteger dataType) throws ContractException {
        final Function function = new Function(FUNC_GETWATERDATABYUSERIDANDTYPE,
                Arrays.<Type>asList(new Uint256(userId),
                new Uint256(dataType)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Struct0>>() {}));
        return executeCallWithSingleValueReturn(function, DynamicArray.class);
    }

    public TransactionReceipt uploadCompanyCert(BigInteger id, BigInteger userId, String imageUrl, BigInteger createTime) {
        final Function function = new Function(
                FUNC_UPLOADCOMPANYCERT,
                Arrays.<Type>asList(new Uint256(id),
                new Uint256(userId),
                new Utf8String(imageUrl),
                new Uint256(createTime)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] uploadCompanyCert(BigInteger id, BigInteger userId, String imageUrl, BigInteger createTime, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_UPLOADCOMPANYCERT,
                Arrays.<Type>asList(new Uint256(id),
                new Uint256(userId),
                new Utf8String(imageUrl),
                new Uint256(createTime)),
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUploadCompanyCert(BigInteger id, BigInteger userId, String imageUrl, BigInteger createTime) {
        final Function function = new Function(
                FUNC_UPLOADCOMPANYCERT,
                Arrays.<Type>asList(new Uint256(id),
                new Uint256(userId),
                new Utf8String(imageUrl),
                new Uint256(createTime)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple4<BigInteger, BigInteger, String, BigInteger> getUploadCompanyCertInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_UPLOADCOMPANYCERT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple4<BigInteger, BigInteger, String, BigInteger>(

                (BigInteger) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (BigInteger) results.get(3).getValue()
                );
    }

    public DynamicArray<Struct1> getCompanyCertByUserId(BigInteger userId) throws ContractException {
        final Function function = new Function(FUNC_GETCOMPANYCERTBYUSERID,
                Arrays.<Type>asList(new Uint256(userId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Struct1>>() {}));
        return executeCallWithSingleValueReturn(function, DynamicArray.class);
    }

    public Tuple4<BigInteger, BigInteger, String, BigInteger> companyCertByUserId(BigInteger param0, BigInteger param1) throws ContractException {
        final Function function = new Function(FUNC_COMPANYCERTBYUSERID,
                Arrays.<Type>asList(new Uint256(param0),
                new Uint256(param1)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple4<BigInteger, BigInteger, String, BigInteger>(
                (BigInteger) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (BigInteger) results.get(3).getValue());
    }

    public List<WaterDataInsertedEventResponse> getWaterDataInsertedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(WATERDATAINSERTED_EVENT, transactionReceipt);
        ArrayList<WaterDataInsertedEventResponse> responses = new ArrayList<WaterDataInsertedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            WaterDataInsertedEventResponse typedResponse = new WaterDataInsertedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.userId = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.dataType = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeWaterDataInsertedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(WATERDATAINSERTED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeWaterDataInsertedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(WATERDATAINSERTED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public List<CompanyCertUploadedEventResponse> getCompanyCertUploadedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(COMPANYCERTUPLOADED_EVENT, transactionReceipt);
        ArrayList<CompanyCertUploadedEventResponse> responses = new ArrayList<CompanyCertUploadedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            CompanyCertUploadedEventResponse typedResponse = new CompanyCertUploadedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.userId = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeCompanyCertUploadedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(COMPANYCERTUPLOADED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeCompanyCertUploadedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(COMPANYCERTUPLOADED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public static WaterRawCall load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new WaterRawCall(contractAddress, client, credential);
    }

    public static WaterRawCall deploy(Client client, CryptoKeyPair credential, String companyAddress, String managerAddress) throws ContractException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(companyAddress),
                new Address(managerAddress)));
        return deploy(WaterRawCall.class, client, credential, getBinary(client.getCryptoSuite()), encodedConstructor);
    }

    public static class Struct0 extends DynamicStruct {
        public BigInteger id;

        public BigInteger userId;

        public BigInteger dataType;

        public String data;

        public String status;

        public BigInteger time;

        public Struct0(Uint256 id, Uint256 userId, Uint256 dataType, Utf8String data, Utf8String status, Uint256 time) {
            super(id,userId,dataType,data,status,time);
            this.id = id.getValue();
            this.userId = userId.getValue();
            this.dataType = dataType.getValue();
            this.data = data.getValue();
            this.status = status.getValue();
            this.time = time.getValue();
        }

        public Struct0(BigInteger id, BigInteger userId, BigInteger dataType, String data, String status, BigInteger time) {
            super(new Uint256(id),new Uint256(userId),new Uint256(dataType),new Utf8String(data),new Utf8String(status),new Uint256(time));
            this.id = id;
            this.userId = userId;
            this.dataType = dataType;
            this.data = data;
            this.status = status;
            this.time = time;
        }
    }

    public static class Struct1 extends DynamicStruct {
        public BigInteger id;

        public BigInteger userId;

        public String imageUrl;

        public BigInteger createTime;

        public Struct1(Uint256 id, Uint256 userId, Utf8String imageUrl, Uint256 createTime) {
            super(id,userId,imageUrl,createTime);
            this.id = id.getValue();
            this.userId = userId.getValue();
            this.imageUrl = imageUrl.getValue();
            this.createTime = createTime.getValue();
        }

        public Struct1(BigInteger id, BigInteger userId, String imageUrl, BigInteger createTime) {
            super(new Uint256(id),new Uint256(userId),new Utf8String(imageUrl),new Uint256(createTime));
            this.id = id;
            this.userId = userId;
            this.imageUrl = imageUrl;
            this.createTime = createTime;
        }
    }

    public static class WaterDataInsertedEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger id;

        public BigInteger userId;

        public BigInteger dataType;
    }

    public static class CompanyCertUploadedEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger id;

        public BigInteger userId;
    }
}
