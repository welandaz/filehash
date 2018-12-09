# filehash
Calculates hashes of a collection of files and directories

## Example

```
final HashProducer hashProducer = HashProducer
                                      .path(Paths.get("my_path")
                                      .hash(Hash.SHA512)
                                      .byteArraySize(4096);

hashProducer.toFile(Paths.get("result_path")); //root path, where result will be written 
    // OR
final Map<Path, String> hashes = hashProducer.toMap(); // in-memory hashmap with results
```
