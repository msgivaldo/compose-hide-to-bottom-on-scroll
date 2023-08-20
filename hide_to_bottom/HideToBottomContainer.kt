
val LocalScrollStateProvider = staticCompositionLocalOf {
    LazyStaggeredGridState()
}

@Composable
fun LazyStaggeredGridState.isNavigationHidden(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }

    return firstVisibleItemScrollOffset == 0 || remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex < firstVisibleItemIndex
            } else {
                previousScrollOffset < firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun BottomScrollableContainer(
    modifier: Modifier = Modifier,
    scrollState: LazyStaggeredGridState = LocalScrollStateProvider.current,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val isVisible = scrollState.isNavigationHidden()
    BottomNavigationAnimateVisibility(
        modifier = modifier,
        isVisible = isVisible,
        content = content,
    )
}

@Composable
fun BottomNavigationAnimateVisibility(
    modifier: Modifier,
    isVisible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = {
                with(density) { it.dp.roundToPx() }
            },
            animationSpec = tween(
                easing = LinearOutSlowInEasing,
                durationMillis = 500,
            ),
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                easing = LinearOutSlowInEasing,
                durationMillis = 500,
            ),
            targetOffsetY = { it },
        ),
        content = content,
    )
}

val listItems = List(30) { index ->
    "https://picsum.photos/${600 + index}/${600 + index}"//just to get different images
}
val destinationsItems = listOf(
    "First Dest" to Icons.Default.AccountBox,
    "Second Dest" to Icons.Default.AddCircle,
    "third Dest" to Icons.Default.Favorite,
)

@Preview(showBackground = true)
@Composable
fun PlaygroundPreview() {
    val staggeredScrollState = rememberLazyStaggeredGridState()
    CompositionLocalProvider(LocalScrollStateProvider provides staggeredScrollState) {
        CarSpecsTheme {
            Surface {
                var selectedIndex by remember { mutableIntStateOf(0) }
                Box {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier.align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 16.dp,
                        columns = StaggeredGridCells.Fixed(2),
                        state = staggeredScrollState,
                        content = {
                            itemsIndexed(items = listItems) { index, item ->
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    AsyncImage(
                                        modifier = Modifier.fillMaxSize(),
                                        model = item,
                                        contentScale = ContentScale.Crop,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                    )
                    BottomScrollableContainer(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    ) {
                        NavigationBar {
                            destinationsItems.forEachIndexed { index, it ->
                                NavigationBarItem(
                                    selected = selectedIndex == index,
                                    onClick = {
                                        selectedIndex = index
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = it.second,
                                            contentDescription = null,
                                        )
                                    },
                                    label = {
                                        Text(text = it.first)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}