# stream 并发
## parallelStream
```java
    public static void main(String[] args) {
        List<Integer> list = IntStream.range(1, 10).boxed().collect(Collectors.toList());
        List<List<Integer>> partition = Lists.partition(list, 2);
//        List<Integer> intList = partition.parallelStream().flatMap(List::stream).collect(Collectors.toList());
        List<Integer> intList = partition.parallelStream()
            .map(ArrayList::new)
            .reduce(new ArrayList<>(), (accumulator, cur) -> {
                cur.addAll(accumulator);
                return cur;
            });
        System.out.println(intList);
    }
```