
public class PageInfo<T> {
    private int pageNum;
    private int pageSize;
    private int size;
    private int startRow;
    private int endRow;
    private int pages;
    private int prePage;
    private int nextPage;
    private boolean isFirstPage;
    private boolean isLastPage;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
    private int navigatePages;
    private List<Integer> [] navigatepageNums;
    private int navigateFirstPage;
    private int navigateLastPage;
    private long total;
    private List<T> list;
}