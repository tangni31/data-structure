class TrieNode(object):
    def __init__(self,val):
        self.val = val
        self.child = {}
        self.isWord = False

class Trie(object):
    def __init__(self):
        self.root = TrieNode('')

    def insert(self,word):
        d = self.root.child
        for i in range(len(word)):#insert new word into trie
            if word[i] in d:
                d = d[word[i]].child
            else: #parent node doesn't contain this key
                for j in range(i,len(word)-1):
                    node = TrieNode(word[j])#create new child nodes
                    d[word[j]] = node
                    d = d[word[j]].child #add new node into its parent's child dic
                node = TrieNode(word[-1]) #the last letter of word
                node.isWord = True #set isWord to True to indicate this node contain a word
                d[word[-1]] = node
                break

    def search(self,word): # True if the word is in the trie, else False
        d = self.root.child
        for i in range(len(word)-1):
            if word[i] in d:
                d = d[word[i]].child
            else: return False
        if word[-1] in d and d[word[-1]].isWord: return True
        return False

    def startWith(self,prefix):#True if there is any word in the trie that starts with the prefix.
        d = self.root.child
        for p in prefix:
            if p in d:
                d = d[p].child
            else: return False
        return True

# Test
tire = Trie()
tire.insert('app')

if tire.search('app'): print ('T')
else: print('F')

if tire.startWith('ap'): print ('T')
else: print('F')
