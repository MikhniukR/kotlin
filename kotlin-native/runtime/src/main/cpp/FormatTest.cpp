/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "Format.h"

#include <array>

#include "gtest/gtest.h"
#include "gmock/gmock.h"

#include "cpp_support/Span.hpp"

using namespace kotlin;

TEST(FormatTest, FormatToSpan_String) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(buffer, "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), buffer.size() - 2);
}

TEST(FormatTest, FormatToSpan_StringFormat) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(buffer, "%s", "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), buffer.size() - 2);
}

TEST(FormatTest, FormatToSpan_IntFormat) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(buffer, "%d", 42);
    EXPECT_THAT(buffer, testing::ElementsAre('4', '2', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), buffer.size() - 2);
}

TEST(FormatTest, FormatToSpan_String_Size0) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(0), "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('\1', '\1', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data());
    EXPECT_THAT(result.size(), 0);
}

TEST(FormatTest, FormatToSpan_StringFormat_Size0) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(0), "%s", "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('\1', '\1', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data());
    EXPECT_THAT(result.size(), 0);
}

TEST(FormatTest, FormatToSpan_IntFormat_Size0) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(0), "%d", 42);
    EXPECT_THAT(buffer, testing::ElementsAre('\1', '\1', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data());
    EXPECT_THAT(result.size(), 0);
}

TEST(FormatTest, FormatToSpan_String_Size1) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(1), "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('\0', '\1', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data());
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_StringFormat_Size1) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(1), "%s", "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('\0', '\1', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data());
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_IntFormat_Size1) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(1), "%d", 42);
    EXPECT_THAT(buffer, testing::ElementsAre('\0', '\1', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data());
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_String_Size2) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(2), "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', '\0', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 1);
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_StringFormat_Size2) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(2), "%s", "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', '\0', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 1);
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_IntFormat_Size2) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(2), "%d", 42);
    EXPECT_THAT(buffer, testing::ElementsAre('4', '\0', '\1', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 1);
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_String_Size3) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(3), "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_StringFormat_Size3) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(3), "%s", "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_IntFormat_Size3) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(3), "%d", 42);
    EXPECT_THAT(buffer, testing::ElementsAre('4', '2', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), 1);
}

TEST(FormatTest, FormatToSpan_String_Size4) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(4), "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), 2);
}

TEST(FormatTest, FormatToSpan_StringFormat_Size4) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(4), "%s", "ab");
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), 2);
}

TEST(FormatTest, FormatToSpan_IntFormat_Size4) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    auto result = FormatToSpan(std_support::span(buffer).first(4), "%d", 42);
    EXPECT_THAT(buffer, testing::ElementsAre('4', '2', '\0', '\1', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 2);
    EXPECT_THAT(result.size(), 2);
}

TEST(FormatTest, FormatToSpan_Sequence) {
    std::array buffer{'\1', '\1', '\1', '\1', '\1'};
    std_support::span<char> result(buffer);
    result = FormatToSpan(result, "a");
    result = FormatToSpan(result, "%s", "b");
    result = FormatToSpan(result, "%d", 4);
    EXPECT_THAT(buffer, testing::ElementsAre('a', 'b', '4', '\0', '\1'));
    EXPECT_THAT(result.data(), buffer.data() + 3);
    EXPECT_THAT(result.size(), 2);
}
