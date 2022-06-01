package com.dejanvuk.parser.Utility;

/**
 * This class is only used for LRU functionality
 * The actual values at a specific key are still stored in a hashmap for performance reasons
 */
public class MessageNodeList {
    private MessageNode head = null;
    private MessageNode tail = null;
    private int size = 0;

    public class MessageNode {
        String key;
        MessageNode prev = null;
        MessageNode next = null;

        public MessageNode(String key) {
            this.key = key;
        }
    }

    public MessageNodeList(){
        this.size = 0;
        this.head = null;
        this.tail = null;
    }

    public int getSize(){
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * TO-DO: Remove this, we will use the hash map to test for existence of the key
     * @param key
     * @return
     */
    public boolean containsMessage(String key) {
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
    public void insertMessageFirst(String key) {
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

    public void insertMessageLast(String key) {
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

    public void insertMessageAt(String key, int position) {
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

    public void removeFirstMessage() {
        if(head != null){
            head = head.next;
            head.prev = null;

            size--;
        }
        else {
            System.out.println("Error adding to message at the start: Message list is empty!");
        }
    }

    public void removeLastMessage() {
        if(tail != null){
            tail = tail.prev;
            tail.next = null;

            size--;
        }
        else {
            System.out.println("Error adding to message at the end: Message list is empty!");
        }
    }

    public void removeMessageAt(int position) {
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
    public void removeMessageWithKey(String key) {
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
                    return;
                }
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