// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

public final class Comment {
    public long id;
    public String username;
    public String subject;
    public String email;
    public String blobKey;
    public String imageAnalyseResult;
    public double sentiment;

    public Comment(long id, String username, String subject) {
        this.id = id;
        this.username = username;
        this.subject = subject;
    }

    public Comment(long id, String username, String subject, String email, String blobKey,
                      String imageAnalyseResult, double sentiment) {
        this(id, username, subject);
        this.email = email;
        this.blobKey = blobKey;
        this.imageAnalyseResult = imageAnalyseResult;
        this.sentiment = sentiment;
    }
}