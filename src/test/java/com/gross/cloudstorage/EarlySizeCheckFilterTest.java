package com.gross.cloudstorage;

import com.gross.cloudstorage.filter.EarlySizeCheckFilter;
import com.gross.cloudstorage.service.StorageProtectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EarlySizeCheckFilterTest {

    @Mock
    private StorageProtectionService storageProtectionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private EarlySizeCheckFilter filter;

    @BeforeEach
    void setUp() {
        DataSize maxRequestSize = DataSize.ofMegabytes(100);
        filter = new EarlySizeCheckFilter(storageProtectionService, maxRequestSize);
    }

    @Test
    void shouldFreeReservedBytesOnFilterChainException() throws Exception {
        long contentLength = 1000L;
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getContentLengthLong()).thenReturn(contentLength);

        doNothing().when(storageProtectionService).assertEnoughSpace(anyLong());
        doNothing().when(storageProtectionService).addReservedSpace(anyLong());
        doThrow(new RuntimeException("Ошибка в цепочке фильтров"))
                .when(filterChain).doFilter(any(), any());

        assertThrows(RuntimeException.class, () ->
                filter.doFilter(request, response, filterChain));

        verify(storageProtectionService).removeReservedSpace(contentLength * 2);
    }

    @Test
    void shouldFreeReservedBytesOnReservationError() throws Exception {
        long contentLength = 1000L;
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getContentLengthLong()).thenReturn(contentLength);

        doThrow(new RuntimeException("Недостаточно места"))
                .when(storageProtectionService).assertEnoughSpace(anyLong());

        filter.doFilter(request, response, filterChain);

        verify(storageProtectionService, never()).addReservedSpace(anyLong());
        verify(storageProtectionService, never()).removeReservedSpace(anyLong());
        verify(filterChain, never()).doFilter(any(), any());
    }
}