import java.math.BigInteger;
import com.activiti.content.storage.fs.PathConverter;

public class ExtractAndPrintPath {
	
	private static  PathConverter converter;
	private static int blockSize = 1024;
	private static int depth = 4;
	private static int storeIdFromDb = 34834;
	
	public static void main(String[] args) {
		
		BigInteger storeId = BigInteger.valueOf(storeIdFromDb);
		converter = new PathConverter();
        converter.setBlockSize(blockSize);
        converter.setIterationDepth(depth);
        System.out.println(converter.getPathForId(storeId));
        
	}

}
