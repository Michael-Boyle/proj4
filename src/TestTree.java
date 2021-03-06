import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * 
 * @author conor cook, zach garner, michael boyle
 *
 */
@SuppressWarnings("unused")
public class TestTree {

	
	private RandomAccessFile file;
	
	private final int INT_BYTES = 4;
	private final int LONG_BYTES = 8;
	private final int CHAR_BYTES = 2;
	
	private final long DEGREE_INDEX = 0;
	private final long SEQ_LENGTH_INDEX = DEGREE_INDEX + INT_BYTES;
	private final long ROOT_INDEX = SEQ_LENGTH_INDEX + INT_BYTES;
	private final long END_INDEX = ROOT_INDEX + LONG_BYTES;
		
	
	public TestTree(String path) throws FileNotFoundException {
		File f = new File(path);
		if (!f.exists())
			throw new FileNotFoundException();
		this.file = new RandomAccessFile(f, "rw");
	}
	
	public TestTree(String path, int degree, int sequenceLength) throws FileAlreadyExistsException {
		File f = new File(path);
		if (f.isFile()) {
		    throw new FileAlreadyExistsException("File already exists");         
		}
		try {
			f.createNewFile();
			this.file = new RandomAccessFile(f, "rw");
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDegree(degree);
		setSeqLength(sequenceLength);
		setEnd(END_INDEX);
		
		setRoot(new Node());
	}
	
	
	public void insert(String key) {
		Tuple<Node, Integer> result = maybeSearch(key);
		Node resNode = result.l();
		Integer resIndex = result.r();
				
		ArrayList<Sequence> elems = resNode.getElems();
		
		if (resIndex != null) {
			elems.get(search(key, resNode)).duplicate();
			resNode.setElems(elems);
		}
		else {
			elems.add(keyIndex(elems, key), new Sequence(key));
			resNode.setElems(elems);
		}
		splitChild(resNode);
	}
	
	public Sequence search(String key) {
		Tuple<Node, Integer> result = maybeSearch(key);
		Node resNode = result.l();
		Integer resIndex = result.r();
		if (resIndex == null)
			return null;
		else
			return resNode.getElems().get(resIndex);
	}
	
	
	private Tuple<Node, Integer> maybeSearch(String key) {
		return maybeSearchHelper(key, getRoot());
	}
	
	private Tuple<Node, Integer> maybeSearchHelper(String key, Node curNode) {
		Integer resIndex = search(key, curNode);
		if (resIndex != null)
			return new Tuple<Node, Integer>(curNode, resIndex);
		else if (isLeaf(curNode))
			return new Tuple<Node, Integer>(curNode, null);
		else
			return maybeSearchHelper(key, curNode.getChildren().get(keyIndex(curNode.getElems(), key)));
	}
	
	private void splitChild(Node n) {
		if (n.getNumElems() >= 2 * getDegree() - 1) {
			ArrayList<Sequence> elems = n.getElems();
			ArrayList<Node> children = n.getChildren();
			
			int middleIndex = Math.floorDiv(elems.size(), 2);
			Sequence middleSeq = elems.get(middleIndex);
			ArrayList<Sequence> leftElems = (ArrayList<Sequence>) elems.subList(0, middleIndex);
			ArrayList<Sequence> rightElems = (ArrayList<Sequence>) elems.subList(middleIndex + 1, elems.size() - 1);
			ArrayList<Node> leftChildren = (ArrayList<Node>) children.subList(0, middleIndex);
			ArrayList<Node> rightChildren = (ArrayList<Node>) children.subList(middleIndex + 1, children.size() - 1);
			
			Node parent;
			if (getRoot().equals(n)) {
				ArrayList<Sequence> newRoot = new ArrayList<Sequence>();
				newRoot.add(middleSeq);
				parent = new Node();
				parent.setElems(newRoot);
				setRoot(parent);
			}
			else 
				parent = n.getParent();
			ArrayList<Sequence> parentElems = parent.getElems();
			ArrayList<Node> parentChildren = parent.getChildren();
			
			Node newLeft = new Node(n.getIndex());
			newLeft.setParent(parent);
			newLeft.setElems(leftElems);
			Node newRight = new Node();
			newRight.setParent(parent);
			newRight.setElems(rightElems);
			
			for (Node ln : leftChildren) 
				ln.setParent(newLeft);
			for (Node rn : rightChildren) 
				rn.setParent(newRight);
			newLeft.setChildren(leftChildren);
			newRight.setChildren(rightChildren);

			parentElems.add(keyIndex(parentElems, middleSeq.getKey()), middleSeq);
			parentChildren.remove(n);
			parentChildren.add(keyIndex(parentElems, leftElems.get(0).getKey()), newLeft);
			parentChildren.add(keyIndex(parentElems, rightElems.get(0).getKey()), newRight);
			
			parent.setElems(parentElems);
			parent.setChildren(parentChildren);
			
			splitChild(parent);
		}
	}
	
	
	public Node getRoot() {
		Node root = null;
		try {
			file.seek(ROOT_INDEX);
			long rootIndex = file.readLong();
			root = new Node(rootIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return root;
	}
	
	private void setRoot(Node n) {
		try {
			file.seek(ROOT_INDEX);
			file.writeLong(n.getIndex());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Integer getDegree() {
		Integer degree = null;
		try {
			file.seek(DEGREE_INDEX);
			degree = file.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	
	private void setDegree(int d) {
		try {
			file.seek(DEGREE_INDEX);
			file.writeInt(d);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Long getEnd() {
		Long end = null;
		try {
			file.seek(END_INDEX);
			end = file.readLong();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return end;
	}
	
	private void setEnd(Long end) {
		try {
			file.seek(END_INDEX);
			file.writeLong(end);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Integer seqLength() {
		Integer length = null;
		try {
			file.seek(SEQ_LENGTH_INDEX);
			length = file.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return length;
	}
	
	private void setSeqLength(int length) {
		try {
			file.seek(SEQ_LENGTH_INDEX);
			file.writeInt(length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private int keyIndex(ArrayList<Sequence> elems, String key) {
		for (int i = 0; i < elems.size(); i++) {
			if (key.compareTo(elems.get(i).getKey()) < 0)
				return i;
		}
		return elems.size();
	}
	
	private Integer search(String key, Node n) {
		ArrayList<Sequence> elems = n.getElems();
		for (int i = 0; i < elems.size(); i++) {
			if (elems.get(i).getKey().equals(key))
				return i;
		}
		return null;
	}
	
	private boolean isLeaf(Node n) {
		ArrayList<Node> children = n.getChildren();
		return children.size() == 0;
	}
	
	
	private class Node {
		
		
		long index;
		
		private final long PARENT_OFFSET = 0;
		private final long NUM_CHILDREN_OFFSET = PARENT_OFFSET + LONG_BYTES;
		private final long NUM_ELEMS_OFFSET = NUM_CHILDREN_OFFSET + INT_BYTES;
		private final long CHILDREN_START = NUM_ELEMS_OFFSET + INT_BYTES;
		private final long ELEMS_START = CHILDREN_START + LONG_BYTES * 2 * getDegree();
		private final long END_OFFSET = ELEMS_START + (CHAR_BYTES + INT_BYTES) * (2 * getDegree() + 1);

		
		private Node() {
			this.index = getEnd();
			setEnd(getEnd() + END_OFFSET);
			setNumChildren(0);
			setNumElems(0);
		}
		
		private Node(long index) {
			this.index = index;
			if (index == getEnd())
				setEnd(getEnd() + END_OFFSET);
			setNumChildren(0);
			setNumElems(0);
		}
		
		
		private Node getParent() {
			Long parentIndex = null;
			try {
				file.seek(index + PARENT_OFFSET);
				parentIndex = file.readLong();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new Node(parentIndex);
		}
		
		private void setParent(Node parent) {
			try {
				file.seek(index + PARENT_OFFSET);
				file.writeLong(parent.getIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private int getNumChildren() {
			Integer numChildren = null;
			try {
				file.seek(index + NUM_CHILDREN_OFFSET);
				numChildren = file.readInt();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return numChildren;
		}
		
		private void setNumChildren(int num) {
			try {
				file.seek(index + NUM_CHILDREN_OFFSET);
				file.writeInt(num);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private int getNumElems() {
			Integer numElems = 0;
			try {
				file.seek(index + NUM_ELEMS_OFFSET);
				numElems = file.readInt();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return numElems;

		}
		
		private void setNumElems(int num) {
			try {
				file.seek(index + NUM_ELEMS_OFFSET);
				file.writeInt(num);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private ArrayList<Node> getChildren() {
			ArrayList<Node> children = new ArrayList<Node>();
			try {
				int numChildren = getNumChildren();
				file.seek(index + CHILDREN_START);
				for (int i = 0; i < numChildren; i++) {
					children.add(new Node(file.readLong()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return children;
		}
		
		private void setChildren(ArrayList<Node> children) {
			try {
				file.seek(index + CHILDREN_START);
				for (int i = 0; i < children.size(); i++) {
					file.writeLong(children.get(i).getIndex());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			setNumChildren(children.size());			
		}
		
		private ArrayList<Sequence> getElems() {
			ArrayList<Sequence> elems = new ArrayList<Sequence>();
			try {
				int numElems = getNumElems();
				file.seek(index + ELEMS_START);
				for (int i = 0; i < numElems; i++) {
					String s = "";
					for (int j = 0; j < seqLength(); j++) {
						s += file.readChar();
					}
					int duplicates = file.readInt();
					elems.add(new Sequence(s, duplicates));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return elems;
		}
		
		private void setElems(ArrayList<Sequence> elems) {
			try {
				file.seek(index + ELEMS_START);
				for (int i = 0; i < elems.size(); i++) {
					Sequence elem = elems.get(i);
					file.writeChars(elem.getKey());
					file.writeInt(elem.getDuplicates());
				}
				file.seek(index + ELEMS_START);
			} catch (IOException e) {
				e.printStackTrace();
			}
			setNumElems(elems.size());
		}
		
		private long getIndex() {
			return this.index;
		}

		
	}
	

	public String toString() {
		StringBuilder b = new StringBuilder();
		try {
			file.seek(0);
			for (int i = 0; i < getEnd(); i++) {
				b.append(file.readByte());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b.toString();
	}
	
	
}




