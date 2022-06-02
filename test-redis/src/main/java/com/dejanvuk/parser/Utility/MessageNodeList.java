package com.dejanvuk.parser.Utility;

import java.util.List;
import java.util.Map;

/**
 * This class is only used for LRU functionality
 * The actual values at a specific key are still stored in a hashmap for performance reasons
 */
public class MessageNodeList {
    private MessageNode head = null;
    private MessageNode tail = null;
    private int size = 0; // current number of keys stored

    // For LRU support
    private int capacity = 1; // default max capacity
    private MessageNode LRUNode = null;
    private MessageNode MRUNode = null;
    Map<String, Value> db = null;

    public MessageNodeList(){
        this.size = 0;
        this.head = null;
        this.tail = null;
    }

    public MessageNodeList(int capacity, Map<String, Value> db) {
        this.capacity = capacity;
        this.db = db;
    }

    /* --------- LRU METHODS--------- */

    /**
     * Updates the node list after a get command was processed in the main loop
     * @param key
     */
    public void get(String key) {
        // we dont have to check if the db contains the key in here,
        // as we already check in the main loop that handles the get command before calling this method
        MessageNode node = db.get(key).getMessageNode();
        makeNodeMRU(node);
    }

    /**
     * If size exceeds the capacity, removes the key from the db
     * @param key
     * @return
     */
    public MessageNode put(String key) {
        if(db.containsKey(key)) {
            MessageNode node = db.get(key).getMessageNode();
            makeNodeMRU(node);
            return node;
        }
        else { // new node, add it
            MessageNode newNode = new MessageNode(key);
            newNode.prev = MRUNode;

            if(MRUNode == null) {
                MRUNode = newNode;
                LRUNode = newNode;
            }
            else {
                MRUNode.next = newNode;
                MRUNode = newNode;
            }

            // If size exceeds the capacity, removes the key from the db
            if(size == capacity) { // evict the least recently used key
                db.remove(LRUNode.key);
                LRUNode = LRUNode.next;
                LRUNode.prev = null;
            }
            else
                size++;

            return newNode;
        }
    }

    /**
     * the key is already removed from the db map prior to this
     * simply remove it from the DLL list with regards to MRU and LRU node
     * @param key
     */
    public void delete(String key) {
        MessageNode curr = LRUNode; // LRU node is always the head

        for(int i = 1 ; i <= size; i++) {
            if(curr.key == key) {
                if(i == 1) {
                    // change the LRU
                    LRUNode = LRUNode.next;
                    LRUNode.prev = null;
                }
                else if (i == size) {
                    // change the MRU
                    MRUNode = MRUNode.prev;
                    MRUNode.next = null;
                }
                else {
                    curr.prev.next = curr.next;
                    curr.next.prev = curr.prev;
                }
                size--;
                return;
            }
            else {
                curr = curr.next;
            }
        }
    }

    public String getMRUKey() {
        return MRUNode.key;
    }

    public void makeNodeMRU(MessageNode node) {
        if(node == MRUNode) return;

        node.next.prev = node.prev;

        if(node != LRUNode) node.prev.next = node.next;
        else LRUNode = node.next;

        node.next = null;
        node.prev = MRUNode;

        MRUNode.next = node;

        MRUNode = node;
    }


    /* --------- LRU METHODS--------- */

    public class MessageNode {
        String key;
        MessageNode prev = null;
        MessageNode next = null;

        public MessageNode(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public int getSize(){
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * TODO: Remove this, we will use the hash map to test for existence of the key
     * @param key
     * @return
     */
    private boolean containsMessage(String key) {
        MessageNode node = head;
        for(int i = 0; i < size; i++){
            if(node.key.equals(key)){
                return true;
            }
            node = node.next;
        }

        return false;
    }

    /**
     * Insert new message at the head
     * @param key
     */
    private void insertMessageFirst(String key) {
        MessageNode node = new MessageNode(key);

        if (head == null) {
            head = node;
            tail = node;
        }
        else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    private void insertMessageLast(String key) {
        MessageNode node = new MessageNode(key);

        if (tail == null) {
            head = node;
            tail = node;
        }
        else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
        this.size++;
    }

    private void insertMessageAt(String key, int position) {
        MessageNode curr = head;

        if(position > size) {
            System.out.println("Position in list exceeds the size!");
            return;
        }
        else if(position == (size + 1)) {
            insertMessageLast(key);
            return;
        }
        else if(curr == null || position == 1) {
            insertMessageFirst(key);
            return;
        }

        for(int i = 2; i <= size; i++) {
            if(i == position) {
                MessageNode newNode = new MessageNode(key);
                newNode.prev = curr;
                newNode.next = curr.next;
                MessageNode temp = curr.next;
                curr.next = newNode;
                temp.prev = newNode;

                size++;
                return;
            }
            else {
                curr = curr.next;
            }
        }
    }

    private void removeFirstMessage() {
        if(head != null){
            head = head.next;
            head.prev = null;

            size--;
        }
        else {
            System.out.println("Error adding to message at the start: Message list is empty!");
        }
    }

    private void removeLastMessage() {
        if(tail != null){
            tail = tail.prev;
            tail.next = null;

            size--;
        }
        else {
            System.out.println("Error adding to message at the end: Message list is empty!");
        }
    }

    private void removeMessageAt(int position) {
        if(position > size) {
            System.out.println("Error removing message: Index exceeds the list's size!");
            return;
        }
        else if(head == null) {
            System.out.println("Error removing message: Cannot remove from an empty list!");
            return;
        }
        else if (position == 1) {
            removeFirstMessage();
            return;
        }
        else if (position == size) {
            removeLastMessage();
            return;
        }

        MessageNode curr = head.next;

        for(int i = 2; i < size; i++) {
            if(i == position) {
                curr.prev.next = curr.next;
                curr.next.prev = curr.prev;

                size--;
                return;
            }
            else {
                curr = curr.next;
            }
        }

    }

    /**
     * first use the hashmap to check if the key exists
     * @param key
     */
    private void removeMessageWithKey(String key) {
        MessageNode curr = head;

        for(int i = 1 ; i <= size; i++) {
            if(curr.key == key) {
                if(i == 1) {
                    removeFirstMessage();
                }
                else if (i == size) {
                    removeLastMessage();
                }
                else {
                    curr.prev.next = curr.next;
                    curr.next.prev = curr.prev;

                    size--;
                }
                return;
            }
            else {
                curr = curr.next;
            }
        }
    }

    public void printKeys() {
        MessageNode curr = head;

        if(size == 0) {
            System.out.println("List is empty!");
            return;
        }

        for(int i = 1 ; i <= size; i++) {
            System.out.println(curr.key);
            curr = curr.next;
        }
    }
}